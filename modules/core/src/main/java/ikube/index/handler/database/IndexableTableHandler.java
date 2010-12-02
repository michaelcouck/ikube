package ikube.index.handler.database;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.content.ByteOutputStream;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.handler.Handler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
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
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class IndexableTableHandler extends Handler {

	private IContentProvider<IndexableColumn> contentProvider;

	public IndexableTableHandler() {
		this.contentProvider = new ColumnContentProvider();
	}

	@Override
	public List<Thread> handle(final IndexContext indexContext, final Indexable<?> indexable) throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
			IndexableTable indexableTable = (IndexableTable) indexable;
			for (int i = 0; i < getThreads(); i++) {
				final IndexableTable cloneIndexableTable = (IndexableTable) SerializationUtilities.clone(indexableTable);
				final Connection connection = indexableTable.getDataSource().getConnection();
				Thread thread = new Thread(new Runnable() {
					public void run() {
						handleTable(indexContext, cloneIndexableTable, connection, null);
					}
				}, IndexableTableHandler.class.getSimpleName() + "." + i);
				thread.start();
				threads.add(thread);
			}
		}
		return threads;
	}

	protected void handleTable(IndexContext indexContext, IndexableTable indexableTable, Connection connection, Document document) {
		List<Indexable<?>> children = indexableTable.getChildren();
		ResultSet resultSet = null;
		try {
			resultSet = getResultSet(indexContext, indexableTable, connection);
			do {
				if (resultSet == null) {
					break;
				}
				// Set the column types
				setColumnTypes(children, resultSet);
				// Set the id field if this is a primary table
				if (indexableTable.isPrimary()) {
					document = new Document();
					setIdField(children, indexableTable, document, resultSet);
				}
				// Handle all the columns that are 'normal', i.e. that don't have references
				// to the values in other columns, like the attachment that needs the name
				// from the name column
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					Indexable<?> indexable = children.get(i - 1);
					if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
						break;
					}
					IndexableColumn indexableColumn = (IndexableColumn) indexable;
					Object object = resultSet.getObject(indexableColumn.getName());
					indexableColumn.setObject(object);
					if (indexableColumn.getNameColumn() != null) {
						continue;
					}
					handleColumn(indexableColumn, document);
				}
				// Handle all the columns that rely on another column, like the attachment
				// column that needs the name from the name column to get the content type
				// for the parser
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					Indexable<?> indexable = children.get(i - 1);
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
					if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
						IndexableTable childIndexableTable = (IndexableTable) indexable;
						handleTable(indexContext, childIndexableTable, connection, document);
					}
				}
				// Add the document to the index if this is the primary table
				if (indexableTable.isPrimary()) {
					indexContext.getIndexWriter().addDocument(document);
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
				logger.error("", e);
			}
			DatabaseUtilities.close(resultSet);
			DatabaseUtilities.close(statement);
		}
		// Once we finish all the results in the primary table
		// then we can close the connection too
		if (indexableTable.isPrimary()) {
			DatabaseUtilities.close(connection);
		}
	}

	protected synchronized ResultSet getResultSet(IndexContext indexContext, IndexableTable indexableTable, Connection connection)
			throws Exception {
		try {
			long idNumber = 0;

			if (indexableTable.isPrimary()) {
				idNumber = indexContext.getIdNumber();
				long minId = getIdFunction(indexableTable, connection, "min");
				if (idNumber < minId) {
					idNumber = minId;
				}
				indexContext.setIdNumber(idNumber + indexContext.getBatchSize());
			}

			String sql = buildSql(indexContext, indexableTable, idNumber);
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			setParameters(indexableTable, preparedStatement);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (!resultSet.next()) {
				if (indexableTable.isPrimary()) {
					long maxId = getIdFunction(indexableTable, connection, "max");
					DatabaseUtilities.close(resultSet);
					DatabaseUtilities.close(preparedStatement);
					if (idNumber > maxId) {
						return null;
					}
					return getResultSet(indexContext, indexableTable, connection);
				}
				return null;
			}
			return resultSet;
		} finally {
			notifyAll();
		}
	}

	protected synchronized void setParameters(IndexableTable indexableTable, PreparedStatement preparedStatement) {
		try {
			List<Indexable<?>> children = indexableTable.getChildren();
			for (Indexable<?> child : children) {
				if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
					IndexableColumn indexableColumn = (IndexableColumn) child;
					if (indexableColumn.getForeignKey() != null) {
						IndexableColumn foreignKey = indexableColumn.getForeignKey();
						try {
							Object parameter = foreignKey.getObject();
							// logger.debug("Parameter : " + parameter);
							preparedStatement.setObject(1, parameter);
						} catch (SQLException e) {
							logger.error("Exception setting the parameters : " + indexableTable + ", " + indexableTable.getChildren(), e);
						}
					}
				}
			}
		} finally {
			notifyAll();
		}
	}

	protected synchronized String buildSql(IndexContext indexContext, IndexableTable indexableTable, long idNumber) throws Exception {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("select ");
			List<Indexable<?>> children = indexableTable.getChildren();
			boolean first = Boolean.TRUE;
			for (Indexable<?> child : children) {
				if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
					IndexableColumn indexableColumn = (IndexableColumn) child;
					if (!first) {
						builder.append(", ");
					} else {
						first = Boolean.FALSE;
					}
					builder.append(indexableTable.getName());
					builder.append(".");
					builder.append(indexableColumn.getName());
				}
			}
			builder.append(" from ");
			builder.append(indexableTable.getName());

			if (indexableTable.getPredicate() != null) {
				builder.append(" ");
				builder.append(indexableTable.getPredicate());
			}

			if (!indexableTable.isPrimary()) {
				for (Indexable<?> child : children) {
					if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
						IndexableColumn indexableColumn = (IndexableColumn) child;
						if (indexableColumn.getForeignKey() != null) {
							if (indexableTable.getPredicate() == null) {
								builder.append(" where ");
							} else {
								builder.append(" and ");
							}
							builder.append(indexableTable.getName());
							builder.append(".");
							builder.append(indexableColumn.getName());
							builder.append(" = ");
							builder.append("?");
							break;
						}
					}
				}
			}

			if (indexableTable.isPrimary()) {
				if (indexableTable.getPredicate() == null) {
					builder.append(" where ");
				} else {
					builder.append(" and ");
				}
				String idColumnName = getIdColumn(indexableTable.getChildren()).getName();
				builder.append(idColumnName);
				builder.append(" > ");
				builder.append(idNumber);
				builder.append(" and ");
				builder.append(idColumnName);
				builder.append(" <= ");
				builder.append(idNumber + indexContext.getBatchSize());
				logger.info(Logging.getString("Sql : ", builder.toString(), ", thread : ", Thread.currentThread().hashCode()));
			}

			return builder.toString();
		} finally {
			notifyAll();
		}
	}

	protected synchronized long getIdFunction(IndexableTable indexableTable, Connection connection, String function) throws Exception {
		try {
			IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());

			long maxId = 0;
			Statement statement = null;
			ResultSet resultSet = null;

			try {
				StringBuilder builder = new StringBuilder("select ");
				builder.append(function);
				builder.append("(");
				builder.append(idColumn.getName());
				builder.append(") from ");
				builder.append(indexableTable.getName());
				statement = connection.createStatement();
				resultSet = statement.executeQuery(builder.toString());
				if (resultSet.next()) {
					maxId = resultSet.getLong(1);
				}
			} finally {
				DatabaseUtilities.close(resultSet);
				DatabaseUtilities.close(statement);
			}

			return maxId;
		} finally {
			notifyAll();
		}
	}

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

	protected void setIdField(List<Indexable<?>> children, IndexableTable indexableTable, Document document, ResultSet resultSet)
			throws Exception {
		IndexableColumn idColumn = getIdColumn(children);
		StringBuilder builder = new StringBuilder();
		builder.append(indexableTable.getName());
		builder.append(".");
		builder.append(idColumn.getName());
		builder.append(".");
		builder.append(resultSet.getObject(idColumn.getName()));

		String id = builder.toString();
		IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
	}

	protected void setColumnTypes(List<Indexable<?>> children, ResultSet resultSet) throws Exception {
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			Indexable<?> indexable = children.get(i - 1);
			// Tables are at the end of the list so once we
			// get to the tables then we will exit the loop
			if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				continue;
			}
			int columnType = resultSetMetaData.getColumnType(i);
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			indexableColumn.setColumnType(columnType);
		}
	}

}