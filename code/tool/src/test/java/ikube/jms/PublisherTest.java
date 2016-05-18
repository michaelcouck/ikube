package ikube.jms;

import ikube.AbstractTest;
import ikube.jms.connect.WeblogicConnector;
import mockit.Mock;
import mockit.MockClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Spy;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.naming.NamingException;
import java.util.Arrays;
import java.util.List;

import static mockit.Mockit.setUpMocks;
import static mockit.Mockit.tearDownMocks;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class PublisherTest extends AbstractTest {

    @Spy
    private Publisher publisher;
    private JmsTemplate jmsTemplate = mock(JmsTemplate.class);

    @SuppressWarnings("UnusedDeclaration")
    @MockClass(realClass = WeblogicConnector.class)
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
            return jmsTemplate;
        }

    }

    @Before
    public void before() throws Exception {
        setUpMocks(this.new WeblogicMock());
    }

    @After
    public void after() {
        tearDownMocks();
    }

    @Test
    @Ignore
    @SuppressWarnings("unchecked")
    public void publishText() {
        publisher.publishText(jmsTemplate, "payload", null, null, 1);
        verify(publisher, times(1)).publish(any(JmsTemplate.class), any(List.class), any(String[].class), any(String[].class));

        publisher.publishText(jmsTemplate, "payload", null, null, 100);
        verify(publisher, times(2)).publish(any(JmsTemplate.class), any(List.class), any(String[].class), any(String[].class));

        publisher.publishText(jmsTemplate, "payload", null, null, 1000);
        verify(publisher, times(12)).publish(any(JmsTemplate.class), any(List.class), any(String[].class), any(String[].class));
    }

    @Test
    @Ignore
    @SuppressWarnings("unchecked")
    public void publishFiles() {
        publisher.publishFiles(jmsTemplate, "messages", null, null, 1);
        verify(publisher, times(1)).publish(any(JmsTemplate.class), any(List.class), any(String[].class), any(String[].class));

        publisher.publishFiles(jmsTemplate, "messages", null, null, 100);
        verify(publisher, times(51)).publish(any(JmsTemplate.class), any(List.class), any(String[].class), any(String[].class));

        publisher.publishFiles(jmsTemplate, "messages", null, null, 1000);
        verify(publisher, times(551)).publish(any(JmsTemplate.class), any(List.class), any(String[].class), any(String[].class));
    }

    @Test
    public void publishMessage() {
        List<String> payloads = Arrays.asList("one", "two", "three");
        publisher.publish(jmsTemplate, payloads, null, null);
        verify(jmsTemplate, times(3)).send(any(MessageCreator.class));
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
                WeblogicConnector.class.getName(), 1, null);
        verify(jmsTemplate, times(1)).send(any(MessageCreator.class));

        publisher.publish(
                "userid",
                "password",
                "url",
                "connection-factory",
                "destination",
                "property",
                "value",
                "payload",
                WeblogicConnector.class.getName(), 10, "messages");
        verify(jmsTemplate, times(11)).send(any(MessageCreator.class));
    }

}