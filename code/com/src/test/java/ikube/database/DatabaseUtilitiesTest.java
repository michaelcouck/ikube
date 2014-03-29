package ikube.database;

import ikube.AbstractTest;
import ikube.toolkit.ObjectToolkit;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 12-10-2010
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

    public static class Url {
        long id;

        public long getId() {
            return id;
        }
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

}
