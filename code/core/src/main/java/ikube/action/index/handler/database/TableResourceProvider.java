package ikube.action.index.handler.database;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TableResourceProvider implements IResourceProvider<ResultSet> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private IndexContext<?> indexContext;
	private IndexableTable indexableTable;
	private DataSource dataSource;
	private AtomicLong currentId;

	TableResourceProvider(final IndexContext<?> indexContext, final IndexableTable indexableTable) throws IOException {
		this.indexContext = indexContext;
		this.indexableTable = indexableTable;
		dataSource = indexableTable.getDataSource();
		currentId = new AtomicLong(0);
		addAllColumns(indexableTable, this.dataSource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized ResultSet getResource() {
		try {
			do {
				setMinAndMaxId(indexableTable, dataSource);
				logger.info("Current id : " + currentId.get() + ", max id : " + indexableTable.getMaximumId());
				ResultSet resultSet = getResultSet(indexContext, indexableTable, currentId);
				if (resultSet.next()) {
					return resultSet;
				}
				DatabaseUtilities.closeAll(resultSet);
				if (currentId.get() > indexableTable.getMaximumId()) {
					// Finished indexing the table hierarchy,
					// no more results from this select statement
					return null;
				}
			} while (true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResources(final List<ResultSet> resources) {
	}

	private void setMinAndMaxId(final IndexableTable indexableTable, final DataSource dataSource) {
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

	private Connection getConnection(final DataSource dataSource) {
		try {
			Connection connection = dataSource.getConnection();
			connection.setAutoCommit(Boolean.FALSE);
			return connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method gets the result set. There are two cases:
	 * 
	 * 1) When the indexable is a top level table the result set is based on the predicate that is defined for the table and the next row in the batch. We use
	 * the column indexables defined in the configuration to build the sql to access the table.<br>
	 * 2) When the indexable is not a top level table then we use the id of the parent table in the sql generation.<br>
	 * 
	 * More detail on how the sql gets generated is in the documentation for the buildSql method.
	 * 
	 * @param indexContext the index context for this index
	 * @param indexableTable the table indexable that is being indexed
	 * @param connection the connection to the database
	 * @return the result set for the table
	 * @throws SQLException
	 */
	private synchronized ResultSet getResultSet(final IndexContext<?> indexContext, final IndexableTable indexableTable, final AtomicLong currentId)
			throws SQLException {
		Connection connection = dataSource.getConnection();
		ResultSet resultSet = null;
		PreparedStatement statement = null;
		try {
			// Get the next id, could be 1 000 000 000 away
			IndexableColumn idColumn = QueryBuilder.getIdColumn(indexableTable.getChildren());
			String nextIdQuery = QueryBuilder.buildNextIdQuery(indexableTable, currentId.get());
			statement = connection.prepareStatement(nextIdQuery);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				Object nextIdObject = resultSet.getObject(idColumn.getName());
				Long nextId = null;
				if (Long.class.isAssignableFrom(nextIdObject.getClass())) {
					nextId = (Long) nextIdObject;
				} else {
					nextId = Long.parseLong(nextIdObject.toString());
				}
				logger.info("Setting next id to : " + nextId);
				currentId.set(nextId.longValue());
				DatabaseUtilities.close(resultSet);
			}

			// Build the sql based on the columns defined in the configuration
			String sql = new QueryBuilder().buildQuery(indexableTable, currentId.get(), indexContext.getBatchSize());
			logger.info("Query : " + sql);
			currentId.set(currentId.get() + indexContext.getBatchSize());
			PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
					ResultSet.CLOSE_CURSORS_AT_COMMIT);
			// Set the parameters if this is a sub table, this typically means that the
			// id from the primary table is set in the prepared statement
			setParameters(indexableTable, preparedStatement);
			return preparedStatement.executeQuery();
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method sets the parameters in the statement. Typically the sub tables need the id from the parent. The sql generated would be something like:
	 * "...where foreignKey = parentId", so we have to get the parent id column and set the parameter.
	 * 
	 * @param indexableTable the table that is being iterated over at the moment, this could be a top level table n which case there will be no foreign key
	 *        references, but in the case of a sub table the parent id will be accessed
	 * @param preparedStatement the statement to set the parameters in
	 * @throws SQLException
	 */
	private void setParameters(final IndexableTable indexableTable, final PreparedStatement preparedStatement) throws SQLException {
		List<Indexable<?>> children = indexableTable.getChildren();
		int parameterIndex = 1;
		for (final Indexable<?> child : children) {
			if (!IndexableColumn.class.isAssignableFrom(child.getClass())) {
				continue;
			}
			IndexableColumn indexableColumn = (IndexableColumn) child;
			if (indexableColumn.getForeignKey() == null) {
				continue;
			}
			IndexableColumn foreignKey = indexableColumn.getForeignKey();
			Object parameter = foreignKey.getContent();
			preparedStatement.setObject(parameterIndex, parameter);
			parameterIndex++;
		}
	}

	/**
	 * This method selects from the specified table using a function, typically something like "max" or "min". In some cases we need to know if we have reached
	 * the end of the table, or what the first id is in the table.
	 * 
	 * @param indexableTable the table to execute the function on
	 * @param connection the database connection
	 * @param function the function to execute on the table
	 * @return the id that resulted from the function
	 * @throws Exception
	 */
	private synchronized long getIdFunction(final IndexableTable indexableTable, final Connection connection, final String function) {
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			long result = 0;
			IndexableColumn idColumn = QueryBuilder.getIdColumn(indexableTable.getChildren());

			if (idColumn == null) {
				logger.warn("Table has no id column : " + indexableTable.getName());
				logger.warn("The results of the indexing are undefined : ");
				return -1;
			}

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
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			DatabaseUtilities.close(resultSet);
			DatabaseUtilities.close(statement);
			notifyAll();
		}
	}

	private void addAllColumns(final IndexableTable indexableTable, final DataSource dataSource) {
		if (indexableTable.isAllColumns()) {
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
			for (final String columnName : columnNames) {
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
		// Now do all the child tables
		for (final Indexable<?> indexable : indexableTable.getChildren()) {
			if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
				addAllColumns((IndexableTable) indexable, dataSource);
			}
		}
	}

	private boolean containsColumn(final IndexableTable indexableTable, final String columnName) {
		if (indexableTable.getChildren() == null) {
			return Boolean.FALSE;
		}
		for (final Indexable<?> child : indexableTable.getChildren()) {
			if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
				if (((IndexableColumn) child).getName().equalsIgnoreCase(columnName)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

}
