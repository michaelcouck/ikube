package ikube.index.visitor.database;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexableTableVisitor<I> extends IndexableVisitor<IndexableTable> {

	private IndexableVisitor<Indexable<?>> indexableColumnVisitor;
	private Comparator<Indexable<?>> childrenComparator = new Comparator<Indexable<?>>() {
		@Override
		public int compare(Indexable<?> o1, Indexable<?> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	@Override
	public void visit(final IndexableTable indexableTable) {
		if (indexableTable.getChildren() == null || indexableTable.getChildren().size() == 0) {
			logger.warn("No columns configured for this table : " + indexableTable.getName());
			return;
		}
		Collections.sort(indexableTable.getChildren(), childrenComparator);
		ResultSet resultSet = null;
		try {
			IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());
			Connection connection = indexableTable.getDataSource().getConnection();
			// First get the id number of the last row to be indexed
			long idNumber = getIdNumber(connection, indexableTable, idColumn);
			// Get the result set starting at the id number specified in the parameter list
			resultSet = getResultSet(connection, indexableTable, idColumn, idNumber);
			do {
				if (!resultSet.next()) {
					idNumber = getIdNumber(connection, indexableTable, idColumn);
					resultSet = getResultSet(connection, indexableTable, idColumn, idNumber);
					if (!resultSet.next()) {
						logger.info("No more results : ");
						break;
					}
				}
				// Thread.sleep(1000);
				doRow(indexableTable, idColumn, resultSet);
			} while (true);
		} catch (Exception e) {
			logger.error("Exception indexing the table : " + indexableTable.getName() + ", " + indexableTable.getSql(), e);
		} finally {
			closeAll(resultSet);
		}
		logger.info("Finished indexing table : " + indexableTable.getName());
	}

	public long getIdNumber(Connection connection, IndexableTable indexableTable, IndexableColumn idColumn) throws Exception {
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		long idNumber = clusterManager.getIdNumber(getIndexContext().getIndexName());
		idNumber = getIdNumber(connection, indexableTable, idColumn, idNumber);
		clusterManager.setIdNumber(getIndexContext().getIndexName(), idNumber + getIndexContext().getBatchSize());
		return idNumber;

	}

	protected long getIdNumber(Connection connection, IndexableTable indexableTable, IndexableColumn idColumn, long idNumber)
			throws Exception {
		if (idNumber == 0) {
			// If the idNumber is 0 then we are the first, so we take the first id in the table
			long minId = getMinId(connection, indexableTable, idColumn);
			idNumber = minId;
		} else {
			// Check that there are results between the id number and the id number + batch size
			long count = getCount(connection, indexableTable, idColumn, idNumber);
			if (count == 0) {
				long maxId = getMaxId(connection, indexableTable, idColumn);
				if (idNumber < maxId) {
					return getIdNumber(connection, indexableTable, idColumn, idNumber + getIndexContext().getBatchSize());
				}
			}
		}
		return idNumber;
	}

	/**
	 * Examples: minimum id value - 4549731 <br>
	 * 1) nextRow = 0, batch = 10 - select * from faq where faqId >= 4549731 and faqId < 4549741<br>
	 * 2) nextRow = 10,batch = 10 - select * from faq where faqId >= 4549741 and faqId < 4549751<br>
	 *
	 * ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY
	 *
	 * @param connection
	 * @param indexableTable
	 * @param idColumn
	 * @param idNumber
	 * @return
	 * @throws Exception
	 */
	public ResultSet getResultSet(Connection connection, IndexableTable indexableTable, IndexableColumn idColumn, long idNumber)
			throws Exception {

		StringBuilder builder = new StringBuilder(indexableTable.getSql());
		if (builder.toString().toLowerCase().contains("where")) {
			builder.append(" and ");
		} else {
			builder.append(" where ");
		}
		builder.append(indexableTable.getName());
		builder.append(".");
		builder.append(idColumn.getName());
		builder.append(" >= ");
		builder.append(idNumber);
		builder.append(" and ");
		builder.append(indexableTable.getName());
		builder.append(".");
		builder.append(idColumn.getName());
		builder.append(" < ");
		builder.append(idNumber + getIndexContext().getBatchSize());

		Statement statement = connection.createStatement();
		logger.info("Sql : " + builder + ", " + Thread.currentThread().hashCode());
		return statement.executeQuery(builder.toString());
	}

	public long getCount(Connection connection, IndexableTable indexableTable, IndexableColumn idColumn, long idNumber) throws Exception {
		long count = 0;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			StringBuilder builder = new StringBuilder("select count(*) from ");
			builder.append(indexableTable.getName());
			builder.append(" where ");
			builder.append(indexableTable.getName());
			builder.append(".");
			builder.append(idColumn.getName());
			builder.append(" > ");
			builder.append(idNumber);
			builder.append(" and ");
			builder.append(indexableTable.getName());
			builder.append(".");
			builder.append(idColumn.getName());
			builder.append(" < ");
			builder.append(idNumber + getIndexContext().getBatchSize());
			statement = connection.createStatement();
			resultSet = statement.executeQuery(builder.toString());
			if (resultSet.next()) {
				count = resultSet.getLong(1);
			}
		} finally {
			close(resultSet);
			close(statement);
		}
		return count;
	}

	public long getMinId(Connection connection, IndexableTable indexableTable, IndexableColumn idColumn) throws Exception {
		// If the idNumber is 0 then we are the first, so we take the first id in the table
		long minId = 0;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			StringBuilder builder = new StringBuilder("select min(");
			builder.append(idColumn.getName());
			builder.append(") from ");
			builder.append(indexableTable.getName());
			statement = connection.createStatement();
			resultSet = statement.executeQuery(builder.toString());
			if (resultSet.next()) {
				minId = resultSet.getLong(1);
			}
		} finally {
			close(resultSet);
			close(statement);
		}
		return minId;
	}

	public long getMaxId(Connection connection, IndexableTable indexableTable, IndexableColumn idColumn) throws Exception {
		long maxId = 0;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			StringBuilder builder = new StringBuilder("select max(");
			builder.append(idColumn.getName());
			builder.append(") from ");
			builder.append(indexableTable.getName());
			statement = connection.createStatement();
			resultSet = statement.executeQuery(builder.toString());
			if (resultSet.next()) {
				maxId = resultSet.getLong(1);
			}
		} finally {
			close(resultSet);
			close(statement);
		}

		return maxId;
	}

	@SuppressWarnings("unchecked")
	public void doRow(final IndexableTable indexableTable, IndexableColumn idColumn, ResultSet resultSet) throws Exception {
		List<Indexable<?>> children = indexableTable.getChildren();
		Object rowId = resultSet.getObject(idColumn.getName());

		if (logger.isDebugEnabled()) {
			if (resultSet.getRow() % 10 == 0) {
				StringBuilder builder = new StringBuilder("Id : ").append(rowId).append(", row : ").append(resultSet.getRow()).append(
						", thread : ").append(Thread.currentThread().hashCode());
				logger.debug(builder.toString());
			}
		}

		Document document = new Document();
		String id = new StringBuilder(indexableTable.getName()).append(".").append(idColumn.getName()).append(".").append(rowId).toString();
		addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
		((IndexableColumnVisitor) indexableColumnVisitor).setDocument(document);
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			String columnName = resultSetMetaData.getColumnName(i);
			int index = getColumnIndex(children, columnName);
			if (index < 0) {
				continue;
			}
			IndexableColumn indexableColumn = (IndexableColumn) children.get(index);
			int columnType = resultSetMetaData.getColumnType(i);
			Object object = resultSet.getObject(i);
			indexableColumn.setColumnType(columnType);
			indexableColumn.setObject(object);
			indexableColumn.accept(indexableColumnVisitor);
		}
		getIndexContext().getIndexWriter().addDocument(document);
	}

	public IndexableColumn getIdColumn(List<Indexable<?>> indexableColumns) {
		for (Indexable<?> indexable : indexableColumns) {
			if (((IndexableColumn) indexable).isIdColumn()) {
				return (IndexableColumn) indexable;
			}
		}
		logger.warn("No id column defined for table : " + indexableColumns);
		return null;
	}

	public int getColumnIndex(List<Indexable<?>> list, String name) {
		int low = 0;
		int high = list.size() - 1;
		name = name.toLowerCase();
		while (low <= high) {
			int mid = (low + high) >>> 1;
			Indexable<?> midVal = list.get(mid);
			String midValName = midVal.getName().toLowerCase();
			int cmp = midValName.compareTo(name);
			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else
				return mid;
		}
		return -(low + 1);
	}

	protected void closeAll(ResultSet resultSet) {
		Statement statement = null;
		Connection connection = null;
		try {
			if (resultSet != null) {
				statement = resultSet.getStatement();
			}
			if (statement != null) {
				connection = statement.getConnection();
			}
		} catch (Exception e) {
			logger.error("Exception getting the statement and connection from the result set : ", e);
		}
		close(resultSet);
		close(statement);
		close(connection);
	}

	protected void close(Statement statement) {
		if (statement == null) {
			return;
		}
		try {
			// logger.info("Closing statement : " + statement);
			statement.close();
		} catch (Exception e) {
			logger.error("Exception closing the statement : ", e);
		}
	}

	protected void close(Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			// logger.info("Closing connection : " + connection);
			connection.close();
		} catch (Exception e) {
			logger.error("Exception closing the connection : ", e);
		}
	}

	protected void close(ResultSet resultSet) {
		if (resultSet == null) {
			return;
		}
		try {
			// logger.info("Closing result set : " + resultSet);
			resultSet.close();
		} catch (Exception e) {
			logger.error("Exception closing the result set : ", e);
		}

	}

	public IndexableVisitor<Indexable<?>> getIndexableColumnVisitor() {
		return indexableColumnVisitor;
	}

	public void setIndexableColumnVisitor(IndexableVisitor<Indexable<?>> indexableColumnVisitor) {
		this.indexableColumnVisitor = indexableColumnVisitor;
	}

}
