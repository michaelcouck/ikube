package ikube.index.handler.database;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.handler.IStrategy;
import ikube.index.handler.IndexableHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * This class performs the indexing of tables. It is the primary focus of Ikube. This class is essentially a database crawler, and is multi
 * threaded. Because Ikube is clusterable it means that there are two levels of threading, within this Jvm and within the cluster. The
 * cluster synchronization is done using the {@link IClusterManager}.
 * 
 * This is just a simple explanation of the table structure and the way the hierarchy is accessed, more information can be found on the
 * Wiki.
 * 
 * Tables are hierarchical, as such the configuration is also and the table handler will recursively call it's self to navigate the
 * hierarchy. The operation is as follows:
 * 
 * 1) Sql will be generated to select the top level table<br>
 * 2) Move to the first row<br>
 * 3) Call it's self, using the id from the first row to select the data from the second table<br>
 * 4) Goto 1(recursively of course)<br>
 * 
 * This allows arbitrarily complex data structures in databases to be indexed.
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableTableHandler extends IndexableHandler<IndexableTable> {

	private ThreadLocal<Integer> exceptions = new ThreadLocal<Integer>();

	/**
	 * This method starts threads and passes the indexable to them. The threads are added to the list of threads that are returned to the
	 * caller that will have to wait for them to finish indexing all the data.
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableTable indexable) throws Exception {
		// We start as many threads to access this table as defined. We return
		// the threads to the caller that they can then wait for the threads to finish
		final DataSource dataSource = indexable.getDataSource();
		List<Future<?>> futures = new ArrayList<Future<?>>();
		exceptions.set(new Integer(0));
		try {
			final AtomicLong currentId = new AtomicLong(0);
			for (int i = 0; i < getThreads(); i++) {
				// Because the transient state data is stored in the indexable during indexing we have
				// to clone the indexable for each thread
				logger.info("Indexable : " + indexable.getName());
				final IndexableTable cloneIndexableTable = (IndexableTable) SerializationUtilities.clone(indexable);
				if (cloneIndexableTable.isAllColumns()) {
					logger.info("Adding all columns to table : " + cloneIndexableTable.getName());
					addAllColumns(cloneIndexableTable, dataSource);
				}
				final Runnable runnable = new Runnable() {
					public void run() {
						try {
							final IContentProvider<IndexableColumn> contentProvider = new ColumnContentProvider();
							setMinAndMaxId(cloneIndexableTable, dataSource);
							handleTable(contentProvider, indexContext, cloneIndexableTable, dataSource, null, currentId);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				};
				final Future<?> future = ThreadUtilities.submit(indexContext.getIndexName(), runnable);
				futures.add(future);
			}
		} catch (Exception e) {
			logger.error("Exception starting the table handler threads : " + indexable.getName(), e);
		}
		return futures;
	}

	protected void addAllColumns(IndexableTable indexableTable, final DataSource dataSource) throws SQLException {
		Connection connection = getConnection(dataSource);
		List<String> columnNames = DatabaseUtilities.getAllColumns(connection, indexableTable.getName());
		List<String> primaryKeyColumns = DatabaseUtilities.getPrimaryKeys(connection, indexableTable.getName());
		if (columnNames.size() == 0) {
			logger.warn("No columns found for table : " + indexableTable.getName());
			return;
		}
		String primaryKeyColumn = primaryKeyColumns.size() > 0 ? primaryKeyColumns.get(0) : columnNames.get(0);
		List<Indexable<?>> children = indexableTable.getChildren();
		if (children == null) {
			children = new ArrayList<Indexable<?>>();
		}
		for (String columnName : columnNames) {
			// We skip the columns that have been explicitly defined in the configuration
			if (containsColumn(indexableTable, columnName)) {
				continue;
			}
			IndexableColumn indexableColumn = new IndexableColumn();
			indexableColumn.setAddress(Boolean.FALSE);
			indexableColumn.setFieldName(columnName);
			indexableColumn.setIdColumn(columnName.equalsIgnoreCase(primaryKeyColumn));
			indexableColumn.setName(columnName);
			indexableColumn.setParent(indexableTable);
			indexableColumn.setAnalyzed(indexableTable.isAnalyzed());
			indexableColumn.setStored(indexableTable.isStored());
			indexableColumn.setVectored(indexableTable.isVectored());
			children.add(indexableColumn);
		}
		indexableTable.setChildren(children);
		DatabaseUtilities.close(connection);
	}

	protected boolean containsPrimaryKeyColumn(IndexableTable indexableTable) {
		for (Indexable<?> child : indexableTable.getChildren()) {
			if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
				if (((IndexableColumn) child).isIdColumn()) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	protected boolean containsColumn(IndexableTable indexableTable, String columnName) {
		if (indexableTable.getChildren() == null) {
			return Boolean.FALSE;
		}
		for (Indexable<?> child : indexableTable.getChildren()) {
			if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
				if (((IndexableColumn) child).getName().equalsIgnoreCase(columnName)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * This method does the actual indexing, and calls it's self recursively.
	 * 
	 * @param indexContext the index context that we are indexing
	 * @param indexableTable the table that we are indexing, this is generally a clone of the original because there is state in the table
	 *        that is used by different threads
	 * @param connection the connection to the database that must be closed when there are no more records left in the top level table
	 * @param document the document that came from the top level table. As we recurse the table hierarchy, we have to pass this document to
	 *        the child tables so they can add their data to the document. When this method is called with the top level table the document
	 *        is null of course
	 * @throws InterruptedException
	 */
	protected void handleTable(final IContentProvider<IndexableColumn> contentProvider, final IndexContext<?> indexContext,
			final IndexableTable indexableTable, final DataSource dataSource, final Document document, final AtomicLong currentId)
			throws Exception {
		Document currentDocument = document == null ? new Document() : document;
		// One connection per thread, the connection will be closed by the thread when finished
		ResultSet resultSet = getResultSet(indexContext, indexableTable, dataSource, currentId);
		while (resultSet != null) {
			try {
				// We have results from the table and we are already on the first result
				List<Indexable<?>> children = indexableTable.getChildren();
				// Set the column types and the data from the table in the column objects
				setColumnTypesAndData(children, resultSet);
				// Set the id field if this is a primary table
				if (indexableTable.isPrimaryTable()) {
					// TODO Create the strategies here and execute them
					setIdField(indexableTable, currentDocument);
				}
				for (Indexable<?> indexable : indexableTable.getChildren()) {
					// Handle all the columns, if any column refers to another column then they
					// must be configured in the correct order so that the name column is before the
					// binary data for the document for example
					if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
						IndexableColumn indexableColumn = (IndexableColumn) indexable;
						handleColumn(contentProvider, indexableColumn, currentDocument);
					} else {
						if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
							IndexableTable childIndexableTable = (IndexableTable) indexable;
							// Here we recursively call this method with the child tables. We pass the document
							// to the child table for this row in the parent table so they can add their fields to the
							// index
							handleTable(contentProvider, indexContext, childIndexableTable, dataSource, currentDocument, currentId);
						}
					}
				}
				// Add the document to the index if this is the primary table
				if (indexableTable.isPrimaryTable()) {
					addDocument(indexContext, indexableTable, currentDocument);
					// TODO Create the strategies here and execute them
					currentDocument = new Document();
				}
				Thread.sleep(indexContext.getThrottle());
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Table indexing teminated : ");
				}
				if (!resultSet.next()) {
					DatabaseUtilities.closeAll(resultSet);
					resultSet = getResultSet(indexContext, indexableTable, dataSource, currentId);
				}
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				logger.error("Exception indexing table : " + indexableTable.getName(), e);
				exceptions.set(new Integer(exceptions.get() + 1));
				if (exceptions.get() > indexableTable.getMaxExceptions()) {
					throw e;
				}
			}
		}
		DatabaseUtilities.closeAll(resultSet);
	}

	@Override
	@SuppressWarnings("hiding")
	public <IndexContext, IndexableColumn> boolean preProcess(IStrategy<IndexContext, IndexableColumn> strategy, IndexContext indexContext,
			IndexableColumn indexableColumn) {
		return strategy.preProcess(indexContext, indexableColumn);
	}

	@Override
	@SuppressWarnings("hiding")
	public <IndexContext, IndexableColumn> boolean postProcess(IStrategy<IndexContext, IndexableColumn> strategy,
			IndexContext indexContext, IndexableColumn indexableColumn) {
		return strategy.postProcess(indexContext, indexableColumn);
	}

	private ResultSet getResultSet(final IndexContext<?> indexContext, final IndexableTable indexableTable, final DataSource dataSource,
			final AtomicLong currentId) throws Exception {
		Connection connection = getConnection(dataSource);
		ResultSet resultSet = getResultSet(indexContext, indexableTable, connection, currentId);
		while (!resultSet.next()) {
			DatabaseUtilities.closeAll(resultSet);
			if (!indexableTable.isPrimaryTable()) {
				return null;
			}
			if (currentId.get() >= indexableTable.getMaximumId()) {
				// Finished indexing the table hierarchy
				return null;
			}
			connection = getConnection(dataSource);
			resultSet = getResultSet(indexContext, indexableTable, connection, currentId);
		}
		return resultSet;
	}

	private Connection getConnection(final DataSource dataSource) throws SQLException {
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(Boolean.FALSE);
		return connection;
	}

	private void setMinAndMaxId(final IndexableTable indexableTable, final DataSource dataSource) throws Exception {
		Connection connection = getConnection(dataSource);
		long minimumId = indexableTable.getMinimumId();
		if (minimumId <= 0) {
			minimumId = getIdFunction(indexableTable, connection, "min");
			indexableTable.setMinimumId(minimumId);
		}
		long maximumId = indexableTable.getMaximumId();
		if (maximumId <= 0) {
			maximumId = getIdFunction(indexableTable, connection, "max");
			indexableTable.setMaximumId(maximumId);
		}
		DatabaseUtilities.close(connection);
	}

	/**
	 * This method gets the result set. There are two cases:
	 * 
	 * 1) When the indexable is a top level table the result set is based on the predicate that is defined for the table and the next row in
	 * the batch. We use the column indexables defined in the configuration to build the sql to access the table.<br>
	 * 2) When the indexable is not a top level table then we use the id of the parent table in the sql generation.<br>
	 * 
	 * More detail on how the sql gets generated is in the documentation for the buildSql method.
	 * 
	 * @param indexContext the index context for this index
	 * @param indexableTable the table indexable that is being indexed
	 * @param connection the connection to the database
	 * @return the result set for the table
	 * @throws Exception
	 */
	protected synchronized ResultSet getResultSet(final IndexContext<?> indexContext, final IndexableTable indexableTable,
			final Connection connection, final AtomicLong currentId) throws Exception {
		try {
			// Build the sql based on the columns defined in the configuration
			String sql = buildSql(indexableTable, indexContext.getBatchSize(), currentId.get());
			if (indexableTable.isPrimaryTable()) {
				currentId.set(currentId.get() + indexContext.getBatchSize());
			}
			PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
					ResultSet.CLOSE_CURSORS_AT_COMMIT);
			// Set the parameters if this is a sub table, this typically means that the
			// id from the primary table is set in the prepared statement for the sub table
			setParameters(indexableTable, preparedStatement);
			return preparedStatement.executeQuery();
		} catch (Exception e) {
			throw e;
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method builds the sql from the columns in the configuration.
	 * 
	 * @param indexableTable the table to generate the sql for
	 * @param batchSize the batch size of the table
	 * @param nextIdNumber the next row id for the table
	 * @return the string sql for the table
	 * @throws Exception
	 */
	protected synchronized String buildSql(final IndexableTable indexableTable, final long batchSize, final long nextIdNumber)
			throws Exception {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("select ");
			List<Indexable<?>> children = indexableTable.getChildren();
			boolean first = Boolean.TRUE;
			// First add all the columns to the sql, this is the same for primary and secondary tables
			for (Indexable<?> child : children) {
				if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
					IndexableColumn indexableColumn = (IndexableColumn) child;
					if (!first) {
						builder.append(", ");
					} else {
						first = Boolean.FALSE;
					}
					builder.append(indexableTable.getName());
					builder.append('.');
					builder.append(indexableColumn.getName());
				}
			}
			builder.append(" from ");
			builder.append(indexableTable.getName());

			// Add the predicate if it exists
			if (indexableTable.getPredicate() != null) {
				builder.append(' ');
				builder.append(indexableTable.getPredicate());
			}

			if (indexableTable.isPrimaryTable()) {
				// If the table is primary then we have to add the predicate that
				// will limit the results to between the next row and the batch size. There
				// could already be a predicate defined also, so add this condition to the
				// existing predicate
				if (indexableTable.getPredicate() == null) {
					builder.append(" where ");
				} else {
					builder.append(" and ");
				}
				String idColumnName = getIdColumn(indexableTable.getChildren()).getName();

				builder.append(indexableTable.getName());
				builder.append('.');
				builder.append(idColumnName);

				builder.append(" >= ");
				builder.append(nextIdNumber);
				builder.append(" and ");

				builder.append(indexableTable.getName());
				builder.append('.');
				builder.append(idColumnName);

				builder.append(" < ");
				builder.append(nextIdNumber + batchSize);
				logger.info(Logging.getString("Sql : ", builder.toString(), " thread : ", Thread.currentThread().hashCode()));
			} else {
				// If this is a sub table then we need to add the condition of the foreign key
				// from the parent table id to the predicate
				for (Indexable<?> child : children) {
					if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
						IndexableColumn indexableColumn = (IndexableColumn) child;
						if (indexableColumn.getForeignKey() == null) {
							continue;
						}
						if (indexableTable.getPredicate() == null) {
							builder.append(" where ");
						} else {
							builder.append(" and ");
						}
						builder.append(indexableTable.getName());
						builder.append('.');
						builder.append(indexableColumn.getName());
						builder.append(" = ");
						builder.append('?');
						break;
					}
				}
			}
			return builder.toString();
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method sets the parameters in the statement. Typically the sub tables need the id from the parent. The sql generated would be
	 * something like: "...where foreignKey = parentId", so we have to get the parent id column and set the parameter.
	 * 
	 * @param indexableTable the table that is being iterated over at the moment, this could be a top level table n which case there will be
	 *        no foreign key references, but in the case of a sub table the parent id will be accessed
	 * @param preparedStatement the statement to set the parameters in
	 */
	protected synchronized void setParameters(final IndexableTable indexableTable, final PreparedStatement preparedStatement)
			throws Exception {
		try {
			List<Indexable<?>> children = indexableTable.getChildren();
			int index = 1;
			for (Indexable<?> child : children) {
				if (!IndexableColumn.class.isAssignableFrom(child.getClass())) {
					continue;
				}
				IndexableColumn indexableColumn = (IndexableColumn) child;
				if (indexableColumn.getForeignKey() == null) {
					continue;
				}
				IndexableColumn foreignKey = indexableColumn.getForeignKey();
				Object parameter = foreignKey.getContent();
				preparedStatement.setObject(index, parameter);
				index++;
			}
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method selects from the specified table using a function, typically something like "max" or "min". In some cases we need to know
	 * if we have reached the end of the table, or what the first id is in the table.
	 * 
	 * @param indexableTable the table to execute the function on
	 * @param connection the database connection
	 * @param function the function to execute on the table
	 * @return the id that resulted from the function
	 * @throws Exception
	 */
	protected synchronized long getIdFunction(final IndexableTable indexableTable, final Connection connection, final String function)
			throws Exception {
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			long result = 0;
			IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());

			StringBuilder builder = new StringBuilder("select ");
			builder.append(function);
			builder.append('(');

			builder.append(indexableTable.getName());
			builder.append('.');
			builder.append(idColumn.getName());

			builder.append(") from ");

			builder.append(indexableTable.getName());

			statement = connection.createStatement();
			resultSet = statement.executeQuery(builder.toString());
			if (resultSet.next()) {
				Object object = resultSet.getObject(1);
				if (object == null) {
					logger.warn("No result from min or max from table : " + indexableTable.getName());
				} else {
					result = Long.class.isAssignableFrom(object.getClass()) ? (Long) object : Long.parseLong(object.toString().trim());
				}
			}
			return result;
		} finally {
			DatabaseUtilities.close(resultSet);
			DatabaseUtilities.close(statement);
			notifyAll();
		}
	}

	/**
	 * Looks through the columns and returns the id column.
	 * 
	 * @param indexableColumns the columns to look through
	 * @return the id column or null if no such column is defined. Generally this will mean a configuration problem, every table must have a
	 *         unique id column
	 */
	protected IndexableColumn getIdColumn(final List<Indexable<?>> indexableColumns) {
		for (Indexable<?> indexable : indexableColumns) {
			if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				continue;
			}
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			if (indexableColumn.isIdColumn()) {
				return indexableColumn;
			}
		}
		logger.warn("No id column defined for table : " + indexableColumns);
		return null;
	}

	/**
	 * This method handles a column. Essentially what this means is that the data from the table is extracted and added to the document, in
	 * the field specified.
	 * 
	 * @param indexable the column to extract the data from and add to the document
	 * @param document the document to add the data to using the field name specified in the column definition
	 */
	protected void handleColumn(final IContentProvider<IndexableColumn> contentProvider, final IndexableColumn indexable,
			final Document document) {
		InputStream inputStream = null;
		OutputStream parsedOutputStream = null;
		ByteOutputStream byteOutputStream = null;
		try {
			String mimeType = null;
			if (indexable.getNameColumn() != null) {
				if (indexable.getNameColumn().getContent() != null) {
					mimeType = indexable.getNameColumn().getContent().toString();
				}
			}

			byteOutputStream = new ByteOutputStream();
			contentProvider.getContent(indexable, byteOutputStream);
			if (byteOutputStream.size() == 0) {
				return;
			}

			byte[] buffer = byteOutputStream.getBytes();
			int length = Math.min(buffer.length, 1024);
			byte[] bytes = new byte[length];

			System.arraycopy(buffer, 0, bytes, 0, bytes.length);

			inputStream = new ByteArrayInputStream(buffer, 0, byteOutputStream.getCount());
			IParser parser = ParserProvider.getParser(mimeType, bytes);
			parsedOutputStream = parser.parse(inputStream, new ByteOutputStream());

			String fieldName = indexable.getFieldName() != null ? indexable.getFieldName() : indexable.getName();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;
			String fieldContent = parsedOutputStream.toString();
			if (indexable.isNumeric()) {
				IndexManager.addNumericField(fieldName, fieldContent, document, store);
			} else {
				IndexManager.addStringField(fieldName, fieldContent, document, store, analyzed, termVector);
			}
		} catch (Exception e) {
			logger.error("Exception accessing the column content : " + byteOutputStream, e);
		} finally {
			FileUtilities.close(inputStream);
			FileUtilities.close(parsedOutputStream);
			FileUtilities.close(byteOutputStream);
		}
	}

	/**
	 * Sets the id field for this document. Typically the id for the document in the index is unique in the index but it may not be. It is
	 * always a good idea to have a unique field, but a table may be indexed twice of course, in which case there will be duplicates in the
	 * id fields.
	 * 
	 * @param indexableTable the table to get the id for
	 * @param document the document to set the id field in
	 * @throws Exception
	 */
	protected void setIdField(final IndexableTable indexableTable, final Document document) throws Exception {
		List<Indexable<?>> children = indexableTable.getChildren();
		IndexableColumn idColumn = getIdColumn(children);
		StringBuilder builder = new StringBuilder();

		builder.append(indexableTable.getName());
		builder.append(' ');
		builder.append(idColumn.getName());
		builder.append(' ');
		builder.append(idColumn.getContent());

		String id = builder.toString();
		IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
	}

	/**
	 * This method sets the data from the table columns in the column objects as well as the type which is gotten from the result set emta
	 * data.
	 * 
	 * @param children the children indexables of the table object
	 * @param resultSet the result set for the table
	 * @throws Exception
	 */
	protected void setColumnTypesAndData(final List<Indexable<?>> children, final ResultSet resultSet) throws Exception {
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		int index = 1;
		for (Indexable<?> indexable : children) {
			if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				continue;
			}
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			int columnType = resultSetMetaData.getColumnType(index);
			Object object = resultSet.getObject(indexableColumn.getName());
			indexableColumn.setColumnType(columnType);
			indexableColumn.setContent(object);
			index++;
		}
	}

}