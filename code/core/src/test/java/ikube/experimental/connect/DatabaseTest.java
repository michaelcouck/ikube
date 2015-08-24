package ikube.experimental.connect;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import ikube.AbstractTest;
import ikube.experimental.Context;
import ikube.experimental.listener.IEvent;
import ikube.experimental.listener.IndexWriterEvent;
import ikube.experimental.listener.ListenerManager;
import ikube.experimental.listener.StartDatabaseProcessingEvent;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
public class DatabaseTest extends AbstractTest {

    @Spy
    @InjectMocks
    private DatabaseConnector database;

    @Mock
    private Session session;
    @Mock
    private Connection connection;
    @Mock
    private ResultSet resultSet;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSetMetaData resultSetMetaData;

    @Mock
    private ListenerManager listenerManager;

    @Mock
    private Context context;
    @Mock
    private StartDatabaseProcessingEvent startDatabaseProcessingEvent;

    @Test
    @SuppressWarnings("unchecked")
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

        final AtomicReference atomicReference = new AtomicReference(null);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                atomicReference.set(invocation.getArguments()[0]);
                return null;
            }
        }).when(listenerManager).fire(any(IEvent.class), any(Boolean.class));

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

        Mockito.when(startDatabaseProcessingEvent.getContext()).thenReturn(context);
        Mockito.when(context.getModification()).thenReturn(new Timestamp(System.currentTimeMillis()));

        database.notify(startDatabaseProcessingEvent);
        Mockito.verify(listenerManager, Mockito.times(1)).fire(any(IEvent.class), any(Boolean.class));
        IndexWriterEvent indexWriterEvent = (IndexWriterEvent) atomicReference.get();
        List<Map<Object, Object>> changedRecords = indexWriterEvent.getData();
        assertEquals("1:1", "one", changedRecords.get(0).get("one"));
        assertEquals("3:3", "five", changedRecords.get(2).get("three"));
    }

}