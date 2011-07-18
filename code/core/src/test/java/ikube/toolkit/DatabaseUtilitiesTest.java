package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.model.Url;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DatabaseUtilitiesTest {

	private ResultSet resultSet;
	private Statement statement;
	private Connection connection;

	private boolean resultSetOpen = Boolean.TRUE;
	private boolean statementOpen = Boolean.TRUE;
	private boolean connectionOpen = Boolean.TRUE;

	@Before
	public void before() throws Exception {
		resultSet = mock(ResultSet.class);
		statement = mock(Statement.class);
		connection = mock(Connection.class);

		when(resultSet.getStatement()).thenReturn(statement);
		when(statement.getConnection()).thenReturn(connection);
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				resultSetOpen = Boolean.FALSE;
				return null;
			}

		}).when(resultSet).close();
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				statementOpen = Boolean.FALSE;
				return null;
			}

		}).when(statement).close();
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				connectionOpen = Boolean.FALSE;
				return null;
			}

		}).when(connection).close();
	}

	@Test
	public void closeAll() {
		DatabaseUtilities.closeAll(resultSet);
		assertFalse(resultSetOpen);
		assertFalse(statementOpen);
		assertFalse(connectionOpen);
	}

	@Test
	public void setIdFieldGetIdFieldValueGetIdFieldName() {
		Long id = System.nanoTime();
		Url url = new Url();
		DatabaseUtilities.setIdField(url, id);
		assertEquals("The id should have been set : ", url.getId(), id);

		Object idField = DatabaseUtilities.getIdFieldValue(url);
		assertEquals("The id field should be found : ", idField, id);

		String idFieldName = DatabaseUtilities.getIdFieldName(Url.class);
		assertEquals("The id field is 'id' : ", "id", idFieldName);
	}

}
