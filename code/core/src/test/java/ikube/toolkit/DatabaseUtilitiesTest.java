package ikube.toolkit;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.ATest;

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
public class DatabaseUtilitiesTest extends ATest {

	private ResultSet resultSet = mock(ResultSet.class);
	private Statement statement = mock(Statement.class);
	private Connection connection = mock(Connection.class);

	private boolean resultSetOpen = Boolean.TRUE;
	private boolean statementOpen = Boolean.TRUE;
	private boolean connectionOpen = Boolean.TRUE;

	@Before
	public void before() throws Exception {
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

}
