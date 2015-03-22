package ikube.jms;

import ikube.AbstractTest;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.jms.*;
import javax.naming.InitialContext;

import static mockit.Mockit.setUpMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class PublisherTest extends AbstractTest {

    @Spy
    private Publisher publisher;

    @org.mockito.Mock
    private Session session;
    @org.mockito.Mock
    private TextMessage message;
    @org.mockito.Mock
    private Connection connection;
    @org.mockito.Mock
    private Destination destination;
    @org.mockito.Mock
    private MessageProducer producer;
    @org.mockito.Mock
    private ConnectionFactory connectionFactory;

    @MockClass(realClass = InitialContext.class)
    class InitialContextMock {
        @Mock
        @SuppressWarnings("UnusedDeclaration")
        public Object lookup(final String name) {
            if (name.contains("connection")) {
                return connectionFactory;
            } else if (name.contains("destination")) {
                return destination;
            }
            throw new RuntimeException("Only connection factory and destination allowed : " + name);
        }
    }

    @Before
    public void before() throws Exception {
        setUpMocks(this.new InitialContextMock());
        when(connectionFactory.createConnection(anyString(), anyString())).thenReturn(connection);
        when(connection.createSession(Boolean.FALSE, TopicSession.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createProducer(destination)).thenReturn(producer);
        when(session.createTextMessage(anyString())).thenReturn(message);
    }

    @Test
    public void publish() throws Exception {
        publisher.publish("userid", "password", "connection-factory", "destination", "jms-client.xml", "property", "value");
        verify(producer, times(1)).send(any(Destination.class), any(Message.class));
    }

}