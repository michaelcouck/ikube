package ikube.toolkit.datageneration;

import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DataGeneratorTwo extends ADataGenerator {

	private int iterations;
	private int threads;

	public DataGeneratorTwo(int iterations, int threads) {
		this.iterations = iterations;
		this.threads = threads;
	}

	public void generate() throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		// Get all the tables in the configuration
		Map<String, IndexableTable> indexableTables = ApplicationContextManager.getBeans(IndexableTable.class);
		logger.info("Inserting : " + (this.threads * this.iterations * indexableTables.size()) + " records.");
		for (final IndexableTable indexableTable : indexableTables.values()) {
			if (!indexableTable.isPrimary()) {
				continue;
			}
			for (int j = 0; j < this.threads; j++) {
				final Connection connection = indexableTable.getDataSource().getConnection();
				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							PerformanceTester.execute(new PerformanceTester.APerform() {
								@Override
								public void execute() throws Exception {
									IndexableTable clonedIndexableTable = (IndexableTable) SerializationUtilities.clone(indexableTable);
									generate(clonedIndexableTable, connection);
								}
							}, "Data generator two : ", iterations);
						} catch (Exception e) {
							logger.error("", e);
						}
					}
				});
				thread.start();
				threads.add(thread);
			}
		}
		ThreadUtilities.waitForThreads(threads);
	}

	protected void generate(IndexableTable indexableTable, Connection connection) {
		// Build the insert sql
		StringBuilder builder = new StringBuilder();
		builder.append("insert into ");
		builder.append(indexableTable.getSchema());
		builder.append(".");
		builder.append(indexableTable.getName());
		builder.append(" (");
		boolean first = Boolean.TRUE;
		for (Indexable<?> indexable : indexableTable.getChildren()) {
			if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				if (((IndexableColumn) indexable).isIdColumn()) {
					continue;
				}
				if (first) {
					first = Boolean.FALSE;
				} else {
					builder.append(", ");
				}
				IndexableColumn indexableColumn = (IndexableColumn) indexable;
				builder.append(indexableColumn.getName());
			}
		}
		builder.append(") values (");
		// Add the values to the sql
		first = Boolean.TRUE;
		for (Indexable<?> indexable : indexableTable.getChildren()) {
			if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				if (((IndexableColumn) indexable).isIdColumn()) {
					continue;
				}
				if (first) {
					first = Boolean.FALSE;
				} else {
					builder.append(", ");
				}
				builder.append("?");
			}
		}
		builder.append(")");
		PreparedStatement preparedStatement = null;
		ResultSet ids = null;
		try {
			// Get the prepared statement
			// logger.info("Sql : " + builder.toString());
			preparedStatement = connection.prepareStatement(builder.toString(), Statement.RETURN_GENERATED_KEYS);
			// Set the parameters based on the index column's column class
			int parameterIndex = 1;
			for (Indexable<?> indexable : indexableTable.getChildren()) {
				if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
					IndexableColumn indexableColumn = (IndexableColumn) indexable;
					if (indexableColumn.isIdColumn()) {
						continue;
					}
					Object parameter = null;
					// If this column is a foreign key reference from the parent table
					// then get the primary key from the parent and set it
					if (indexableColumn.getForeignKey() == null) {
						String columnClass = indexableColumn.getColumnClass();
						int columnLength = indexableColumn.getColumnLength();
						try {
							parameter = instanciateObject(Class.forName(columnClass), columnLength);
						} catch (ClassNotFoundException e) {
							logger.error("", e);
						}
					} else {
						parameter = indexableColumn.getForeignKey().getObject();
					}
					// logger.info("Parameter : " + parameter);
					preparedStatement.setObject(parameterIndex, parameter);
					parameterIndex++;
				}
			}
			preparedStatement.executeUpdate();

			List<Indexable<?>> children = indexableTable.getChildren();
			if (containsSubTables(children)) {
				ids = preparedStatement.getGeneratedKeys();
				while (ids.next()) {
					Object rowid = ids.getObject(1);
					if (!Number.class.isAssignableFrom(rowid.getClass())) {
						continue;
					}
					long id = ids.getLong(1);
					// logger.info("Id : " + id + ", " + indexableTable.getName());
					// Set the id in the parent table
					IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());
					idColumn.setObject(id);
					// Do the child tables
					for (Indexable<?> indexable : children) {
						if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
							generate((IndexableTable) indexable, connection);
						}
					}
				}
			}
			connection.commit();
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			DatabaseUtilities.close(ids);
			DatabaseUtilities.close(preparedStatement);
		}
	}

	protected boolean containsSubTables(List<Indexable<?>> children) {
		for (Indexable<?> child : children) {
			if (IndexableTable.class.isAssignableFrom(child.getClass())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
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

}
