package ikube.toolkit;

import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class DataGeneratorTwo {

	private Logger logger = Logger.getLogger(this.getClass());
	private List<String> words;
	private String wordsFilePath = "/data/words.txt";
	private int iterations;
	private int threads;

	public DataGeneratorTwo(int iterations, int threads) {
		this.iterations = iterations;
		this.threads = threads;
		this.words = new ArrayList<String>();
		InputStream inputStream = this.getClass().getResourceAsStream(wordsFilePath);
		String words = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
		StringTokenizer tokenizer = new StringTokenizer(words);
		while (tokenizer.hasMoreTokens()) {
			this.words.add(tokenizer.nextToken());
		}
	}

	public List<Thread> generate(String configLocation) throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		// Get all the tables in the configuration
		ApplicationContextManager.getApplicationContext(configLocation);
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
		return threads;
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
						parameter = getParameter(columnClass, columnLength);
					} else {
						parameter = indexableColumn.getForeignKey().getObject();
					}
					preparedStatement.setObject(parameterIndex, parameter);
					parameterIndex++;
				}
			}
			preparedStatement.executeUpdate();

			ids = preparedStatement.getGeneratedKeys();
			while (ids.next()) {
				long id = ids.getInt(1);
				// Set the id in the parent table
				IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());
				idColumn.setObject(id);
				// Do the child tables
				for (Indexable<?> indexable : indexableTable.getChildren()) {
					if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
						generate((IndexableTable) indexable, connection);
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

	protected Object getParameter(String columnClass, int columnLength) {
		if (Boolean.class.getName().equals(columnClass)) {
			return Boolean.TRUE;
		} else if (Integer.class.getName().equals(columnClass)) {
			return new Integer((int) System.nanoTime());
		} else if (Long.class.getName().equals(columnClass)) {
			return new Long(System.nanoTime());
		} else if (Timestamp.class.getName().equals(columnClass)) {
			return new Timestamp(System.currentTimeMillis());
		} else if (String.class.getName().equals(columnClass)) {
			return generateText(columnLength * 5, columnLength);
		} else if (Blob.class.getName().equals(columnClass)) {
			return new ByteArrayInputStream(generateText(columnLength * 5, columnLength).getBytes());
		}
		return null;
	}

	protected String generateText(int count, int maxLength) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			int index = (int) (Math.random() * (words.size() - 1));
			String word = this.words.get(index);
			builder.append(word);
			builder.append(" ");
		}
		if (builder.length() > maxLength) {
			return builder.substring(0, maxLength);
		}
		return builder.toString();
	}

	public static void main(String[] args) throws Exception {
		String configLocation = "/data/spring.xml";
		DataGeneratorTwo dataGeneratorTwo = new DataGeneratorTwo(1000, 3);
		List<Thread> threads = dataGeneratorTwo.generate(configLocation);
		ThreadUtilities.waitForThreads(threads);
		System.out.println("Finished generation : ");
	}

}
