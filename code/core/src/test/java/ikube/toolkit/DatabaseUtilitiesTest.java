package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.model.Url;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DatabaseUtilitiesTest extends ATest {

	private ResultSet resultSet;
	private Statement statement;
	private Connection connection;
	private DataSource dataSource;

	public DatabaseUtilitiesTest() {
		super(DatabaseUtilitiesTest.class);
	}

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
		DatabaseUtilities.setIdField(url, id);
		assertEquals("The id should have been set : ", url.getId(), id);

		Object idField = DatabaseUtilities.getIdFieldValue(url);
		assertEquals("The id field should be found : ", idField, id);

		String idFieldName = DatabaseUtilities.getIdFieldName(Url.class);
		assertEquals("The id field is 'id' : ", "id", idFieldName);
	}

}
