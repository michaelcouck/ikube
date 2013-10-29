package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.IntegrationTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DatabaseUtilitiesIntegration extends IntegrationTest {

	private String nonXaDataSourceH2 = "nonXaDataSourceH2";
	@SuppressWarnings("unused")
	private String nonXaDataSourceDb2 = "nonXaDataSourceDb2";
	@SuppressWarnings("unused")
	private String nonXaDataSourceOracle = "nonXaDataSourceOracle";

	@Test
	public void getAllColumns() throws Exception {
		Connection connection = null;
		List<String> allColumns = null;
		try {
			connection = getConnection(nonXaDataSourceH2);
			String allColumnsString = "[id, timestamp, admin1code, admin2code, admin3code, admin4code, alternatenames, asciiname, cc2, city, country, "
					+ "countrycode, elevation, featureclass, featurecode, geonameid, gtopo30, latitude, longitude, modification, name, population, timezone]";
			allColumns = DatabaseUtilities.getAllColumns(connection, "geoname");
			assertEquals("All the columns from the geoname table : ", allColumnsString, allColumns.toString().toLowerCase());
		} finally {
			DatabaseUtilities.close(connection);
		}
	}

	/** NOTE : This function is not implemented in all drivers. */
	@Test
	public void getForeignKeys() throws Exception {
		Connection connection = null;
		try {
			connection = getConnection(nonXaDataSourceH2);
			List<String[]> foreignKeys = DatabaseUtilities.getForeignKeys(connection, "geoname");
			logger.info("Foreign keys : " + foreignKeys);
		} finally {
			DatabaseUtilities.close(connection);
		}
	}

	/** NOTE : This function is not implemented in all drivers. */
	@Test
	public void getPrimaryKeys() throws Exception {
		Connection connection = null;
		try {
			connection = getConnection(nonXaDataSourceH2);
			List<String> primaryKeyColumns = DatabaseUtilities.getPrimaryKeys(connection, "geoname");
			logger.info(primaryKeyColumns.toString());
		} finally {
			DatabaseUtilities.close(connection);
		}
	}

	public Connection getConnection(final String dataSourceName) throws SQLException {
		DataSource dataSource = ApplicationContextManager.getBean(dataSourceName);
		Connection connection = dataSource.getConnection();
		assertNotNull(connection);
		return connection;
	}

}