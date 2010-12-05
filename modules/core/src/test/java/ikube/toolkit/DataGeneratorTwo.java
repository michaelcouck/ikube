package ikube.toolkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;

public class DataGeneratorTwo {

	public void generate(String configLocation) {
		// Get all the tables in the configuration
		ApplicationContextManager.getApplicationContext(configLocation);
		Map<String, IndexableTable> indexableTables = ApplicationContextManager.getBeans(IndexableTable.class);
		StringBuilder builder = new StringBuilder();
		builder.append("insert into (");
		for (IndexableTable indexableTable : indexableTables.values()) {
			// Build the insert sql
			boolean first = Boolean.TRUE;
			for (Indexable<?> indexable : indexableTable.getChildren()) {
				if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
					if (first) {
						first = Boolean.FALSE;
					} else {
						builder.append(", ");
					}
					IndexableColumn indexableColumn = (IndexableColumn) indexable;
					builder.append(indexableTable.getSchema());
					builder.append(".");
					builder.append(indexableTable.getName());
					builder.append(".");
					builder.append(indexableColumn.getName());
				}
			}
			builder.append(") values (");
			// Add the values to the sql
			first = Boolean.TRUE;
			for (Indexable<?> indexable : indexableTable.getChildren()) {
				if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
					if (first) {
						first = Boolean.FALSE;
					} else {
						builder.append(", ");
					}
					builder.append("?");
				}
			}
			builder.append(")");
			try {
				// Get the prepared statement
				Connection connection = indexableTable.getDataSource().getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(builder.toString());
				// Set the parameters based on the index column's column class
				for (Indexable<?> indexable : indexableTable.getChildren()) {
					if (IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
						IndexableColumn indexableColumn = (IndexableColumn) indexable;
						String columnClass = indexableColumn.getColumnClass();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected Object getObject(String columnClass) {
		if (Integer.class.getName().equals(columnClass)) {
			return new Integer((int) System.nanoTime());
		} else if (Long.class.getName().equals(columnClass)) {
			return new Long(System.nanoTime());
		} else if (Timestamp.class.getName().equals(columnClass)) {
			return new Timestamp(System.currentTimeMillis());
		} else if (String.class.getName().equals(columnClass)) {
			
		}
		return null;
	}

	public static void main(String[] args) {
		String configLocation = "/spring.xml";
		DataGeneratorTwo dataGeneratorTwo = new DataGeneratorTwo();
		dataGeneratorTwo.generate(configLocation);
	}

}
