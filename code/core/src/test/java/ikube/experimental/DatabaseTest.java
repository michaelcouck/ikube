package ikube.experimental;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import ikube.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
public class DatabaseTest extends AbstractTest {

    @Spy
    @InjectMocks
    private Database database;
    @Mock
    private Connection connection;
    @Mock
    private ResultSet resultSet;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSetMetaData resultSetMetaData;
    @Mock
    private Session session;

    @Test
    public void readChangedRecords() throws JSchException, SQLException {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                // Do nothing...
                return Boolean.TRUE;
            }
        }).when(database).createSshTunnel();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                // Do nothing...
                return Boolean.TRUE;
            }
        }).when(database).createDatabaseConnection();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                // Do nothing...
                return Boolean.TRUE;
            }
        }).when(session).disconnect();

        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        Mockito.when(resultSet.getMetaData()).thenReturn(resultSetMetaData, resultSetMetaData, resultSetMetaData);
        Mockito.when(resultSetMetaData.getColumnCount()).thenReturn(3, 3, 3);
        Mockito.when(resultSetMetaData.getColumnName(1)).thenReturn("one");
        Mockito.when(resultSetMetaData.getColumnName(2)).thenReturn("two");
        Mockito.when(resultSetMetaData.getColumnName(3)).thenReturn("three");

        Mockito.when(resultSet.getObject(1)).thenReturn("one", "two", "three");
        Mockito.when(resultSet.getObject(2)).thenReturn("two", "three", "four");
        Mockito.when(resultSet.getObject(3)).thenReturn("three", "four", "five");

        database.notify(null);
        // Assert.assertEquals("1:1", "one", changedRecords.get(0).get("one"));
        // Assert.assertEquals("3:3", "five", changedRecords.get(2).get("three"));
    }

}