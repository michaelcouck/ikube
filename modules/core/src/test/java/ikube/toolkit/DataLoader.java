package ikube.toolkit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.xml.sax.InputSource;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class DataLoader {

	private Logger logger;

	public DataLoader() {
		this.logger = Logger.getLogger(this.getClass());
	}

	/**
	 * Creates the tables for the unit tests.
	 *
	 * @param filePath
	 *            the path to the sql to create the tables
	 */
	public void createTables(String filePath) {
		Connection connection = null;
		try {
			connection = getConnection();
			File createTableFile = FileUtilities.getFile(filePath, Boolean.FALSE);
			String contents = FileUtilities.getContents(createTableFile).toString();
			StringTokenizer tokenizer = new StringTokenizer(contents, ";", Boolean.FALSE);
			while (tokenizer.hasMoreTokens()) {
				String sql = tokenizer.nextToken();
				logger.info("Sql : " + sql);
				boolean executed = connection.createStatement().execute(sql);
				logger.info("Executed : " + executed);
			}
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			releaseConnection(connection);
		}
	}

	/**
	 * Gets the data from the database and writes a DBUnit file to the fiel system with the data set in it.
	 *
	 * @param tableNames
	 *            the names of the tables to select from
	 * @param filePath
	 *            the path to the output file for the data set
	 */
	public void writeDataSet(String[] tableNames, String filePath) {
		Connection connection = null;
		try {
			connection = getConnection();
			IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
			IDataSet dataSet = databaseConnection.createDataSet(tableNames);
			File file = FileUtilities.getFile(filePath, Boolean.FALSE);
			logger.info("Writing data to : " + file.getAbsolutePath() + ", " + Arrays.asList(tableNames));
			OutputStream outputStream = new FileOutputStream(file);
			FlatXmlDataSet.write(dataSet, outputStream);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			releaseConnection(connection);
		}
	}

	public void insertDataSet(String filePath) {
		try {
			IDataSet dataSet = getDataSet(filePath);
			insertDataSet(dataSet);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void insertDataSet(IDataSet dataSet) {
		Connection connection = null;
		try {
			connection = getConnection();
			IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
			DatabaseOperation.INSERT.execute(databaseConnection, dataSet);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			releaseConnection(connection);
		}
	}

	public IDataSet getDataSet(String filePath) {
		try {
			File file = FileUtilities.getFile(filePath, Boolean.FALSE);
			ByteArrayOutputStream byteArrayOutputStream = FileUtilities.getContents(file);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			return getDataSet(byteArrayInputStream);
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

	public IDataSet getDataSet(InputStream inputStream) {
		try {
			InputSource inputSource = new InputSource(inputStream);
			FlatXmlProducer flatXmlProducer = new FlatXmlProducer(inputSource);
			return new FlatXmlDataSet(flatXmlProducer);
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

	public ReplacementDataSet getReplacementDataSet(IDataSet dataSet) {
		return new ReplacementDataSet(dataSet);
	}

	private Connection getConnection() {
		try {
			DataSource dataSource = ApplicationContextManager.getBean(DataSource.class);
			Connection connection = dataSource.getConnection();
			return connection;
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

	private void releaseConnection(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public static void main(String[] args) {
		ApplicationContextManager.getApplicationContext("/data/spring.xml");
		DataLoader dataLoader = new DataLoader();
		dataLoader.writeDataSet(new String[] { "faq", "attachment" }, "./output.xml");
	}

}