package ikube.jms.connect;

import ikube.AbstractTest;
import mockit.Mock;
import mockit.MockClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

import static mockit.Mockit.setUpMocks;
import static mockit.Mockit.tearDownMocks;
import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-06-2015
 */
public class WebSphereConnectorTest extends AbstractTest {

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = InitialContext.class)
    public class InitialContextMock {

        @Mock
        public void $init(final Hashtable<?, ?> environment) {
        }

        @Mock
        public Object lookup(final String name) {
            if (name.contains("connection")) {
                return connectionFactory;
            } else if (name.contains("destination")) {
                return destination;
            }
            throw new RuntimeException("Only connection factory and destination allowed : " + name);
        }

    }

    @org.mockito.Mock
    private Destination destination;
    @org.mockito.Mock
    private ConnectionFactory connectionFactory;

    @Spy
    private WebSphereConnector webSphereConnector;

    @Before
    public void before() {
        setUpMocks(this.new InitialContextMock());
    }

    @After
    public void after() {
        tearDownMocks();
    }

    @Test
    public void connect() throws NamingException {
        JmsTemplate jmsTemplate = webSphereConnector.connect("userid", "password", "url", "connection-factory", "destination");
        assertNotNull("The connection factory must be set : ", jmsTemplate.getConnectionFactory());
        assertNotNull("The default destination must be set from the JNDi initial context : ", jmsTemplate.getDefaultDestination());
    }

}
