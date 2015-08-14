package ikube.toolkit;

import ikube.IntegrationTest;
import ikube.database.DatabaseUtilities;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-10-2010
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
        List<String> allColumns;
        try {
            connection = getConnection(nonXaDataSourceH2);
            String allColumnsString = "[id, timestamp, admin1code, admin2code, admin3code, admin4code, alternatenames, asciiname, cc2, city, country, "
                    + "countrycode, elevation, featureclass, featurecode, geonameid, gtopo30, latitude, longitude, modification, name, population, timezone, version]";
            allColumns = DatabaseUtilities.getAllColumns(connection, "geoname");
            LOGGER.info("All columns : " + allColumns);
            assertTrue("All the columns from the geoname table : ", allColumns.toString().toLowerCase().startsWith(allColumnsString));
        } finally {
            DatabaseUtilities.close(connection);
        }
    }

    /**
     * NOTE : This function is not implemented in all drivers.
     */
    @Test
    public void getForeignKeys() throws Exception {
        Connection connection = null;
        try {
            connection = getConnection(nonXaDataSourceH2);
            List<String[]> foreignKeys = DatabaseUtilities.getForeignKeys(connection, "geoname");
            LOGGER.info("Foreign keys : " + foreignKeys);
        } finally {
            DatabaseUtilities.close(connection);
        }
    }

    /**
     * NOTE : This function is not implemented in all drivers.
     */
    @Test
    public void getPrimaryKeys() throws Exception {
        Connection connection = null;
        try {
            connection = getConnection(nonXaDataSourceH2);
            List<String> primaryKeyColumns = DatabaseUtilities.getPrimaryKeys(connection, "geoname");
            LOGGER.info(primaryKeyColumns.toString());
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