package ikube.jms.connect;

import ikube.AbstractTest;
import mockit.Mock;
import mockit.MockClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

import static mockit.Mockit.setUpMocks;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-06-2015
 */
public class WeblogicTest extends AbstractTest {

    @Spy
    private Weblogic weblogic;

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
    public class InitialContextMock {

        @Mock
        public void $init(Hashtable<?, ?> environment) {
        }

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
    public void before() throws JMSException {
        setUpMocks(this.new InitialContextMock());
        when(connectionFactory.createConnection(anyString(), anyString())).thenReturn(connection);
        when(connection.createSession(Boolean.FALSE, TopicSession.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createProducer(destination)).thenReturn(producer);
        when(session.createTextMessage(anyString())).thenReturn(message);
    }

    @Test
    public void publish() throws NamingException {
        JmsTemplate jmsTemplate = weblogic.connect("userid", "password", "url", "connection-factory", "destination");
        assertNotNull("The connection factory must be set : ", jmsTemplate.getConnectionFactory());
        assertNotNull("The default destination must be set from the JNDi initial context : ", jmsTemplate.getDefaultDestination());
    }

}
