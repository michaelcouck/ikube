package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.Url;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DatabaseUtilitiesTest extends AbstractTest {

	private ResultSet resultSet;
	private Statement statement;
	private Connection connection;
	private DataSource dataSource;

	@Before
	public void before() throws Exception {
		resultSet = mock(ResultSet.class);
		statement = mock(Statement.class);
		connection = mock(Connection.class);
		dataSource = mock(DataSource.class);

		when(resultSet.getStatement()).thenReturn(statement);
		when(statement.getConnection()).thenReturn(connection);
		when(connection.createStatement()).thenReturn(statement);
		when(dataSource.getConnection()).thenReturn(connection);
	}

	@Test
	public void executeStatement() throws Exception {
		DatabaseUtilities.executeStatement(dataSource, "select * from action");
		verify(statement, atLeastOnce()).execute(anyString());
		verify(statement, atLeastOnce()).close();
		verify(connection, atLeastOnce()).close();
	}

	@Test
	public void closeAll() throws Exception {
		DatabaseUtilities.closeAll(resultSet);
		verify(resultSet, atLeastOnce()).close();
		verify(statement, atLeastOnce()).close();
		verify(connection, atLeastOnce()).close();
	}

	@Test
	public void setIdFieldGetIdFieldValueGetIdFieldName() {
		long id = System.nanoTime();
		Url url = new Url();
		ObjectToolkit.setIdField(url, id);
		assertEquals("The id should have been set : ", url.getId(), id);
		Object idField = ObjectToolkit.getIdFieldValue(url);
		assertEquals("The id field should be found : ", idField, id);
		String idFieldName = ObjectToolkit.getIdFieldName(Url.class);
		assertEquals("The id field is 'id' : ", "id", idFieldName);
	}

	@Test
	public void getAllColumns() throws Exception {
		Connection connection = null;
		List<String> allColumns = null;
		try {
			String allColumnsString = "[id, timestamp, admin1code, admin2code, admin3code, admin4code, alternatenames, asciiname, cc2, city, country, countrycode, elevation, featureclass, featurecode, geonameid, gtopo30, latitude, longitude, modification, name, population, timezone]";
			connection = getDb2Connection();
			allColumns = DatabaseUtilities.getAllColumns(connection, "geoname");
			assertEquals("All the columns from the geoname table : ", allColumnsString, allColumns.toString().toLowerCase());
			DatabaseUtilities.close(connection);

			connection = DriverManager.getConnection("jdbc:oracle:thin:@81.95.118.139:1521:xe", "oracle", "Bp0st2013");
			allColumns = DatabaseUtilities.getAllColumns(connection, "geoname");
			logger.info("All columns : " + allColumns);
			assertEquals("All the columns from the geoname table : ", allColumnsString, allColumns.toString().toLowerCase());
			DatabaseUtilities.close(connection);

			// connection = DriverManager.getConnection("jdbc:postgresql://81.95.118.139:5432/cmp", "cmp", "pwd");
			// allColumns = DatabaseUtilities.getAllColumns(connection, "geoname");
			// logger.info("All columns : " + allColumns);
			// String campaignColumns = "[id, campaign_set_id, copied_from_campaign_id, truvo_orderline_pub_id, banner_creation_needed, "
			// + "notes_cmp, more_info_url, more_info_email, status, date_created, date_updated, created_by, updated_by, version, language_id, "
			// + "external_campaign_id, more_work_needed, banner_body_text, banner_set_id]";
			// assertEquals("All the columns from the doctor table : ", campaignColumns, allColumns.toString());
			// DatabaseUtilities.close(connection);
		} finally {
			DatabaseUtilities.close(connection);
		}
	}

	@Test
	public void getForeignKeys() throws Exception {
		Connection connection = null;
		try {
			connection = getDb2Connection();
			List<String[]> foreignKeys = DatabaseUtilities.getForeignKeys(connection, "geoname");
			logger.info("Foreign keys : " + foreignKeys);
		} finally {
			DatabaseUtilities.close(connection);
		}
	}

	@Test
	@Ignore
	public void getPrimaryKeys() throws Exception {
		Connection connection = null;
		try {
			connection = getDb2Connection();
			List<String> primaryKeyColumns = DatabaseUtilities.getPrimaryKeys(connection, "geoname");
			logger.info(primaryKeyColumns.toString());
			assertTrue("Must contain the attachment id column : ", primaryKeyColumns.contains("ATTACHMENTID"));
		} finally {
			DatabaseUtilities.close(connection);
		}
	}
	
	@Test
	@Ignore
	public void getConnection() throws SQLException {
		Connection connection = getDb2Connection();
		assertNotNull(connection);
	}

	private Connection getDb2Connection() throws SQLException {
		return DriverManager.getConnection("jdbc:db2://192.168.1.10:50000/ikube", "db2inst1", "Caherl1ne2013");
		// return DriverManager.getConnection("jdbc:db2://81.95.118.139:50000/icube", "db2", "Bp0st2013");
	}

}
