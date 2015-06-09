package ikube.jms;

import ikube.AbstractTest;
import ikube.jms.connect.Weblogic;
import mockit.Mock;
import mockit.MockClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.naming.NamingException;

import static mockit.Mockit.setUpMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class PublisherTest extends AbstractTest {

    @Spy
    private Publisher publisher;
    private JmsTemplate jmsTemplate;

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = Weblogic.class)
    public class WeblogicMock {

        @Mock
        public void $init() {
        }

        @Mock
        public JmsTemplate connect(
                final String userid,
                final String password,
                final String url,
                final String connectionFactory,
                final String destination) throws NamingException {
            jmsTemplate = mock(JmsTemplate.class);
            return jmsTemplate;
        }

    }

    @Before
    public void before() throws Exception {
        setUpMocks(this.new WeblogicMock());
    }

    @Test
    public void publish() throws Exception {
        publisher.publish(
                "userid",
                "password",
                "url",
                "connection-factory",
                "destination",
                "property",
                "value",
                "payload",
                "ikube.jms.connection.Weblogic");
        verify(jmsTemplate, times(1)).send(any(MessageCreator.class));
    }

}