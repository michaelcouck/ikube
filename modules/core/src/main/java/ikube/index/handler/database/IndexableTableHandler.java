package ikube.index.handler.database;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.handler.IndexableHandler;
import ikube.index.handler.IndexableHandlerType;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.SerializationUtilities;

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

	/** The content provider for column data. */
	private IContentProvider<IndexableColumn> contentProvider;

	public IndexableTableHandler() {
		this.contentProvider = new ColumnContentProvider();
	}

	/**
	 * This method starts threads and passes the indexable to them. The threads are added to the list of threads that are returned to the
	 * caller that will have to wait for them to finish indexing all the data.
	 */
	@Override
	@IndexableHandlerType(type = IndexableTable.class)
	public List<Thread> handle(final IndexContext indexContext, final IndexableTable indexable) throws Exception {
		SerializationUtilities.setTransientFields(IndexableTable.class);
		SerializationUtilities.setTransientFields(IndexableColumn.class);
		// We start as many threads to access this table as defined. We return
		// the threads to the caller that they can then wait for the threads to finish
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < getThreads(); i++) {
			// Because the transient state data is stored in the indexable during indexing we have
			// to clone the indexable for each thread
			final IndexableTable cloneIndexableTable = (IndexableTable) SerializationUtilities.clone(indexable);
			// One connection per thread, the connection will be closed by the thread when finished
			final Connection connection = indexable.getDataSource().getConnection();
			Thread thread = new Thread(new Runnable() {
				public void run() {
					handleTable(indexContext, cloneIndexableTable, connection, null);
				}
			}, IndexableTableHandler.class.getSimpleName() + "." + i);
			thread.start();
			threads.add(thread);
		}
		return threads;
	}

	/**
	 * This method does the actual indexing, and calls it's self recursively.
	 * 
	 * @param indexContext
	 *            the index context that we are indexing
	 * @param indexableTable
	 *            the table that we are indexing, this is generally a clone of the original because there is state in the table that is used
	 *            by different threads
	 * @param connection
	 *            the connection to the database that must be closed when there are no more records left in the top level table
	 * @param document
	 *            the document that came from the top level table. As we recurse the table hierarchy, we have to pass this document to the
	 *            child tables so they can add their data to the document. When this method is called with the top level table the document
	 *            is null of course
	 */
	protected void handleTable(IndexContext indexContext, IndexableTable indexableTable, Connection connection, Document document) {
		ResultSet resultSet = null;
		try {
			resultSet = getResultSet(indexContext, indexableTable, connection);
			do {
				if (resultSet == null) {
					break;
				}
				List<Indexable<?>> children = indexableTable.getChildren();
				// Set the column types and the data from the table in the column objects
				setColumnTypesAndData(children, resultSet);
				// Set the id field if this is a primary table
				if (indexableTable.isPrimary()/* || document == null */) {
					document = new Document();
					setIdField(indexableTable, document);
				}
				// Handle all the columns that are 'normal', i.e. that don't have references
				// to the values in other columns, like the attachment that needs the name
				// from the name column
				for (Indexable<?> indexable : indexableTable.getChildren()) {
					if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
						continue;
					}
					IndexableColumn indexableColumn = (IndexableColumn) indexable;
					if (indexableColumn.getNameColumn() != null) {
						continue;
					}
					handleColumn(indexableColumn, document);
				}

				// Handle all the columns that rely on another column, like the attachment
				// column that needs the name from the name column to get the content type
				// for the parser
				for (Indexable<?> indexable : indexableTable.getChildren()) {
					if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
						break;
					}
					IndexableColumn indexableColumn = (IndexableColumn) indexable;
					if (indexableColumn.getNameColumn() == null) {
						continue;
					}
					handleColumn(indexableColumn, document);
				}

				// Handle all the sub tables
				for (Indexable<?> indexable : children) {
					if (!IndexableTable.class.isAssignableFrom(indexable.getClass())) {
						continue;
					}
					IndexableTable childIndexableTable = (IndexableTable) indexable;
					// Here we recursively call this method with the child tables. We pass the document
					// to the child table for this row in the parent table so they can add their fields to the
					// index
					handleTable(indexContext, childIndexableTable, connection, document);
				}
				// Add the document to the index if this is the primary table
				if (indexableTable.isPrimary()) {
					indexContext.getIndex().getIndexWriter().addDocument(document);
					Thread.sleep(indexContext.getThrottle());
				}
				// Move to the next row in the result set
				if (!resultSet.next()) {
					if (indexableTable.isPrimary()) {
						// We need to see if there are any more results
						resultSet = getResultSet(indexContext, indexableTable, connection);
					} else {
						// If the table is not primary then we have exhausted the results
						break;
					}
				}
			} while (true);
		} catch (Exception e) {
			logger.error("Exception indexing table : " + indexableTable, e);
		} finally {
			// Close the result set and the statement for this
			// table, could be the sub table of course
			Statement statement = null;
			try {
				if (resultSet != null) {
					statement = resultSet.getStatement();
				}
			} catch (Exception e) {
				logger.error("Exception accessing the statement from the result set : " + resultSet, e);
			}
			DatabaseUtilities.close(resultSet);
			DatabaseUtilities.close(statement);
		}
		// Once we finish all the results in the primary table
		// then we can close the connection too. Each thread gets
		// it's own connection so we don't overlap threads/connections
		if (indexableTable.isPrimary()) {
			DatabaseUtilities.close(connection);
		}
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
	 * @param indexContext
	 *            the index context for this index
	 * @param indexableTable
	 *            the table indexable that is being indexed
	 * @param connection
	 *            the connection to the database
	 * @return the result set for the table
	 * @throws Exception
	 */
	protected synchronized ResultSet getResultSet(IndexContext indexContext, IndexableTable indexableTable, Connection connection)
			throws Exception {
		try {
			long nextIdNumber = 0;
			// If this is a primary table then we need to find the first id in the table. For example if we are just
			// starting to access this table then the id number will be 0, but the first id in the table could be 1 234 567,
			// in which case we will have no records, so we need to execute where id > 1 234 567 and < 1 234 567 + batchSize
			if (indexableTable.isPrimary()) {
				IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
				nextIdNumber = clusterManager.getIdNumber(indexContext.getIndexName(), indexableTable.getName(),
						indexContext.getBatchSize());
				long minId = getIdFunction(indexableTable, connection, "min");
				if (nextIdNumber < minId) {
					nextIdNumber = minId;
				}
			}

			// Now we build the sql based on the columns defined in the configuration
			String sql = buildSql(indexableTable, indexContext.getBatchSize(), nextIdNumber);
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			// Set the parameters if this is a sub table, this typically means that the
			// id from the primary table is set in the prepared statement for the sub table
			setParameters(indexableTable, preparedStatement);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (!resultSet.next()) {
				DatabaseUtilities.close(resultSet);
				DatabaseUtilities.close(preparedStatement);
				// No results, if this is a primary table then check that we have
				// reached the end of the table, it could be that the predicate is
				// id > 1 234 567 and < 1 234 567 + batchSize but there are no results
				// between these values, so the next predicate would be
				// id > 1 234 567 + batchSize and < 1 234 567 + (batchSize * 2)
				if (indexableTable.isPrimary()) {
					long maxId = getIdFunction(indexableTable, connection, "max");
					// If we have exhausted the results then we return null and the thread dies
					if (nextIdNumber > maxId) {
						return null;
					}
					// Try the next predicate + batchSize
					return getResultSet(indexContext, indexableTable, connection);
				}
				return null;
			}
			return resultSet;
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method builds the sql from the columns in the configuration.
	 * 
	 * @param indexableTable
	 *            the table to generate the sql for
	 * @param batchSize
	 *            the batch size of the table
	 * @param nextIdNumber
	 *            the next row id for the table
	 * @return the string sql for the table
	 * @throws Exception
	 */
	protected synchronized String buildSql(IndexableTable indexableTable, long batchSize, long nextIdNumber) throws Exception {
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
					builder.append(indexableTable.getSchema());
					builder.append(".");
					builder.append(indexableTable.getName());
					builder.append(".");
					builder.append(indexableColumn.getName());
				}
			}
			builder.append(" from ");
			builder.append(indexableTable.getSchema());
			builder.append(".");
			builder.append(indexableTable.getName());

			// Add the predicate if it exists
			if (indexableTable.getPredicate() != null) {
				builder.append(" ");
				builder.append(indexableTable.getPredicate());
			}

			if (indexableTable.isPrimary()) {
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

				builder.append(indexableTable.getSchema());
				builder.append(".");
				builder.append(indexableTable.getName());
				builder.append(".");
				builder.append(idColumnName);

				builder.append(" >= ");
				builder.append(nextIdNumber);
				builder.append(" and ");

				builder.append(indexableTable.getSchema());
				builder.append(".");
				builder.append(indexableTable.getName());
				builder.append(".");
				builder.append(idColumnName);

				builder.append(" < ");
				builder.append(nextIdNumber + batchSize);
				logger.info(Logging.getString("Sql : ", builder.toString(), ", thread : ", Thread.currentThread().hashCode()));
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
						builder.append(indexableTable.getSchema());
						builder.append(".");
						builder.append(indexableTable.getName());
						builder.append(".");
						builder.append(indexableColumn.getName());
						builder.append(" = ");
						builder.append("?");
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
	 * @param indexableTable
	 *            the table that is being iterated over at the moment, this could be a top level table n which case there will be no foreign
	 *            key references, but in the case of a sub table the parent id will be accessed
	 * @param preparedStatement
	 *            the statement to set the parameters in
	 */
	protected synchronized void setParameters(IndexableTable indexableTable, PreparedStatement preparedStatement) {
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
				try {
					Object parameter = foreignKey.getObject();
					preparedStatement.setObject(index, parameter);
					index++;
				} catch (SQLException e) {
					logger.error("Exception setting the parameters : " + indexableTable + ", " + indexableTable.getChildren(), e);
				}
			}
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method selects from the specified table using a function, typically something like "max" or "min". In some cases we need to know
	 * if we have reached the end of the table, or what the first id is in the table.
	 * 
	 * @param indexableTable
	 *            the table to execute the function on
	 * @param connection
	 *            the database connection
	 * @param function
	 *            the function to execute on the table
	 * @return the id that resulted from the function
	 * @throws Exception
	 */
	protected synchronized long getIdFunction(IndexableTable indexableTable, Connection connection, String function) throws Exception {
		IndexableColumn idColumn;
		long result = 0;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			idColumn = getIdColumn(indexableTable.getChildren());

			StringBuilder builder = new StringBuilder("select ");
			builder.append(function);
			builder.append("(");

			builder.append(indexableTable.getSchema());
			builder.append(".");
			builder.append(indexableTable.getName());
			builder.append(".");
			builder.append(idColumn.getName());

			builder.append(") from ");

			builder.append(indexableTable.getSchema());
			builder.append(".");
			builder.append(indexableTable.getName());

			statement = connection.createStatement();
			resultSet = statement.executeQuery(builder.toString());
			if (resultSet.next()) {
				result = resultSet.getLong(1);
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
	 * @param indexableColumns
	 *            the columns to look through
	 * @return the id column or null if no such column is defined. Generally this will mean a configuration problem, every table must have a
	 *         unique id column
	 */
	protected IndexableColumn getIdColumn(List<Indexable<?>> indexableColumns) {
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
	 * @param indexable
	 *            the column to extract the data from and add to the document
	 * @param document
	 *            the document to add the data to using the field name specified in the column definition
	 */
	protected void handleColumn(IndexableColumn indexable, Document document) {
		InputStream inputStream = null;
		OutputStream parsedOutputStream = null;
		ByteOutputStream byteOutputStream = null;
		try {
			String mimeType = null;
			if (indexable.getNameColumn() != null) {
				if (indexable.getNameColumn().getObject() != null) {
					mimeType = indexable.getNameColumn().getObject().toString();
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
			IndexManager.addStringField(fieldName, fieldContent, document, store, analyzed, termVector);
		} catch (Exception e) {
			logger.error("Exception accessing the column content : " + byteOutputStream.toString(), e);
		} finally {
			close(inputStream);
			inputStream = null;
			close(parsedOutputStream);
			parsedOutputStream = null;
			close(byteOutputStream);
			byteOutputStream = null;
		}
	}

	protected void close(OutputStream outputStream) {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	protected void close(InputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * Sets the id field for this document. Typically the id for the document in the index is unique in the index but it may not be. It is
	 * always a good idea to have a unique field, but a table may be indexed twice of course, in which case there will be duplicates in the
	 * id fields.
	 * 
	 * @param indexableTable
	 *            the table to get the id for
	 * @param document
	 *            the document to set the id field in
	 * @throws Exception
	 */
	protected void setIdField(IndexableTable indexableTable, Document document) throws Exception {
		List<Indexable<?>> children = indexableTable.getChildren();
		IndexableColumn idColumn = getIdColumn(children);
		StringBuilder builder = new StringBuilder();

		builder.append(indexableTable.getSchema());
		builder.append(".");
		builder.append(indexableTable.getName());
		builder.append(".");
		builder.append(idColumn.getName());
		builder.append(".");
		builder.append(idColumn.getObject());

		String id = builder.toString();
		IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
	}

	/**
	 * This method sets the data from the table columns in the column objects as well as the type which is gotten from the result set emta
	 * data.
	 * 
	 * @param children
	 *            the children indexables of the table object
	 * @param resultSet
	 *            the result set for the table
	 * @throws Exception
	 */
	protected void setColumnTypesAndData(List<Indexable<?>> children, ResultSet resultSet) throws Exception {
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
			indexableColumn.setObject(object);
			index++;
		}
	}

}