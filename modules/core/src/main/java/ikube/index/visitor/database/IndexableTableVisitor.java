package ikube.index.visitor.database;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.visitor.IndexableVisitor;
import ikube.model.IndexContext;
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

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

public class IndexableTableVisitor<I> extends IndexableVisitor<IndexableTable> {

	private Logger logger = Logger.getLogger(this.getClass());
	private IndexContext indexContext;
	private IndexableVisitor<Indexable<?>> indexableColumnVisitor;
	private IDataBase dataBase;

	@Override
	public void visit(final IndexableTable indexableTable) {
		if (indexableTable.getChildren() == null || indexableTable.getChildren().size() == 0) {
			logger.warn("No columns configured for this table : " + indexableTable.getName());
			return;
		}
		Collections.sort(indexableTable.getChildren(), new Comparator<Indexable<?>>() {
			@Override
			public int compare(Indexable<?> o1, Indexable<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		ResultSet resultSet = null;
		try {
			IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
			int nextRow = clusterManager.getNextBatchNumber(indexContext);
			IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());
			resultSet = getResultSet(indexableTable, idColumn, nextRow);
			do {
				if (!resultSet.next()) {
					nextRow = clusterManager.getNextBatchNumber(indexContext);
					resultSet = getResultSet(indexableTable, idColumn, nextRow);
					if (!resultSet.next()) {
						logger.info("No more results : ");
						break;
					}
					break;
				}
				// Thread.sleep(250);
				doRow(indexableTable, idColumn, resultSet);
			} while (true);
		} catch (Exception e) {
			logger.error("Exception indexing the table : " + indexableTable.getName() + ", " + indexableTable.getSql(), e);
		} finally {
			close(resultSet);
		}
		logger.info("Finished indexing table : " + indexableTable.getName());
	}

	protected ResultSet getResultSet(IndexableTable indexableTable, IndexableColumn idColumn, long nextRow) throws Exception {
		// ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY
		String predicate = "";// " where " + idColumn.getName() + " > " + nextRow;
		Connection connection = indexableTable.getDataSource().getConnection();
		Statement statement = connection.createStatement();
		String sql = indexableTable.getSql() + predicate;
		logger.info("Sql : " + sql + ", " + Thread.currentThread().hashCode());
		return statement.executeQuery(sql);
	}

	@SuppressWarnings("unchecked")
	protected void doRow(final IndexableTable indexableTable, IndexableColumn idColumn, ResultSet resultSet) throws Exception {
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
			int index = binarySearch(children, columnName);
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
		indexContext.getIndexWriter().addDocument(document);
	}

	protected IndexableColumn getIdColumn(List<Indexable<?>> indexableColumns) {
		for (Indexable<?> indexable : indexableColumns) {
			if (((IndexableColumn) indexable).isIdColumn()) {
				return (IndexableColumn) indexable;
			}
		}
		logger.warn("No id column defined for table : " + indexableColumns);
		return null;
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
