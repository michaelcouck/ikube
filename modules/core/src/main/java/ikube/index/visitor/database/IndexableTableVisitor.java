package ikube.index.visitor.database;

import ikube.database.IDataBase;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ClusterManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;

public class IndexableTableVisitor<I> extends IndexableVisitor<IndexableTable> {

	private Logger logger = Logger.getLogger(this.getClass());
	private IndexContext indexContext;
	private IndexableVisitor<Indexable<?>> indexableColumnVisitor;
	private IDataBase dataBase;

	@Override
	public void visit(final IndexableTable indexableTable) {
		final List<Indexable<?>> indexableColumns = indexableTable.getChildren();
		if (indexableColumns == null) {
			logger.warn("No columns configured for this table : " + indexableTable.getName());
			return;
		}
		Collections.sort(indexableColumns, new Comparator<Indexable<?>>() {
			@Override
			public int compare(Indexable<?> o1, Indexable<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		ResultSet resultSet = null;
		try {
			resultSet = getResultSet(indexableTable);
			long nextRowNumber = moveToBatch(resultSet);
			while (nextRowNumber > 0 && resultSet.next()) {
				// Thread.sleep(250);
				doRow(indexableColumns, resultSet);
				if (resultSet.getRow() >= nextRowNumber) {
					nextRowNumber = moveToBatch(resultSet);
				}
			}
		} catch (Exception e) {
			logger.error("Exception indexing the table : " + indexableTable.getName() + ", " + indexableTable.getSql(), e);
		} finally {
			close(resultSet);
		}
		logger.info("Finished indexing table : " + indexableTable.getName());
	}

	protected int moveToBatch(ResultSet resultSet) throws Exception {
		int nextBatchNumber = ClusterManager.getNextBatchNumber(indexContext);
		int currentRow = resultSet.getRow();
		if (currentRow <= 0) {
			// Move the cursor to the first row
			resultSet.next();
		}
		while (resultSet.getRow() < nextBatchNumber) {
			if (!resultSet.next()) {
				// We return -1 when we get to the end of the results
				return -1;
			}
		}
		int rowAfter = resultSet.getRow();
		if (logger.isInfoEnabled()) {
			StringBuilder builder = new StringBuilder("Next batch number : ").append(nextBatchNumber).append(", moved from : ").append(
					currentRow).append(", to : ").append(rowAfter).append(", runnable : ").append(this);
			logger.info(builder.toString());
		}
		return nextBatchNumber + (int) indexContext.getBatchSize();
	}

	@SuppressWarnings( { "unchecked" })
	protected void doRow(List indexableColumns, ResultSet resultSet) throws Exception {
		logger.debug("Doing row : " + resultSet.getRow() + ", " + Thread.currentThread().hashCode());
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		Document document = new Document();
		((IndexableColumnVisitor) indexableColumnVisitor).setDocument(document);
		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			String columnName = resultSetMetaData.getColumnName(i);
			int index = binarySearch(indexableColumns, columnName);
			if (index < 0) {
				continue;
			}
			IndexableColumn indexableColumn = (IndexableColumn) indexableColumns.get(index);
			int columnType = resultSetMetaData.getColumnType(i);
			Object object = resultSet.getObject(i);
			indexableColumn.setColumnType(columnType);
			indexableColumn.setObject(object);
			indexableColumn.accept(indexableColumnVisitor);
		}
		indexContext.getIndexWriter().addDocument(document);
	}

	protected ResultSet getResultSet(final IndexableTable indexableTable) throws Exception {
		Connection connection = indexableTable.getDataSource().getConnection();
		// NOTE : Refer to the note 05.11.10
		Statement statement = connection.createStatement(/* ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY */);
		String sql = indexableTable.getSql();
		logger.info("Sql : " + sql + ", " + Thread.currentThread().hashCode());
		ResultSet resultSet = statement.executeQuery(sql);
		return resultSet;
	}

	protected int binarySearch(List<Indexable<?>> list, String name) {
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
				return mid; // key found
		}
		return -(low + 1); // key not found
	}

	protected void close(ResultSet resultSet) {
		if (resultSet == null) {
			return;
		}
		Statement statement = null;
		Connection connection = null;
		try {
			statement = resultSet.getStatement();
			connection = statement.getConnection();
		} catch (Exception e) {
			logger.error("Exception getting the connection from the result set : ", e);
		}
		try {
			logger.info("Closing result set : " + resultSet);
			resultSet.close();
		} catch (Exception e) {
			logger.error("Exception closing the result set : ", e);
		}
		if (statement != null) {
			try {
				logger.info("Closing statement : " + statement);
				statement.close();
			} catch (Exception e) {
				logger.error("Exception closing the statement : ", e);
			}
		}
		if (connection != null) {
			try {
				logger.info("Closing connection : " + connection);
				connection.close();
			} catch (Exception e) {
				logger.error("Exception closing the connection : ", e);
			}
		}
	}

	public IndexContext getIndexContext() {
		return indexContext;
	}

	public void setIndexContext(IndexContext indexContext) {
		this.indexContext = indexContext;
	}

	public IndexableVisitor<Indexable<?>> getIndexableColumnVisitor() {
		return indexableColumnVisitor;
	}

	public void setIndexableColumnVisitor(IndexableVisitor<Indexable<?>> indexableColumnVisitor) {
		this.indexableColumnVisitor = indexableColumnVisitor;
	}

	public IDataBase getDataBase() {
		return dataBase;
	}

	public void setDataBase(IDataBase dataBase) {
		this.dataBase = dataBase;
	}

}