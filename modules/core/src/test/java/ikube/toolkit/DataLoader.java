package ikube.toolkit;

import java.io.File;
import java.io.FileInputStream;
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
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.operation.DatabaseOperation;
import org.xml.sax.InputSource;

public class DataLoader {

	private Logger logger = Logger.getLogger(this.getClass());

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

	public void getData(String[] tableNames, String filePath) {
		Connection connection = null;
		try {
			connection = getConnection();
			IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
			// IDatabaseTester databaseTester = new DefaultDatabaseTester(databaseConnection);
			IDataSet dataSet = databaseConnection.createDataSet(tableNames);
			// ITable table = dataSet.getTable("faq");
			// logger.error("Data set : " + dataSet + ", table : " + table);
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

	public void setData(String filePath) {
		Connection connection = null;
		try {
			connection = getConnection();
			IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
			File file = FileUtilities.getFile(filePath, Boolean.FALSE);
			InputStream inputStream = new FileInputStream(file);
			InputSource inputSource = new InputSource(inputStream);
			FlatXmlProducer flatXmlProducer = new FlatXmlProducer(inputSource);
			IDataSet dataSet = new FlatXmlDataSet(flatXmlProducer);
			DatabaseOperation.CLEAN_INSERT.execute(databaseConnection, dataSet);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			releaseConnection(connection);
		}
	}

}
