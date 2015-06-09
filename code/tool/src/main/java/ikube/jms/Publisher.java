package ikube.jms;

import ikube.jms.connect.IConnector;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 * This is a simple JMS client that will publish a message to either a topic or a queue. Parameters
 * can be passed to the publisher.
 * <p/>
 * <pre>
 *     java -jar ikube-tool-5.2.0.jar ikube.jms.Publisher userid password url ConnectionFactory MyTopic property value payload
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class Publisher {

    @SuppressWarnings("UnusedDeclaration")
    private Logger logger = LoggerFactory.getLogger(Publisher.class);

    @Option(name = "-u")
    private String username;
    @Option(name = "-p")
    private String password;
    @Option(name = "-url")
    private String url;
    @Option(name = "-cf")
    private String connectionFactory;
    @Option(name = "-d")
    private String destination;
    @Option(name = "-h")
    private String headerNames;
    @Option(name = "-v")
    private String headerValues;
    @Option(name = "-pl")
    private String payload;
    @Option(name = "-ct")
    private String connection;

    /**
     * The main that will be called from the {@link ikube.Ikube} class.
     *
     * @param args arguments are very specific, and they are all mandatory except for the header names and values. So
     *             to enumerate the required parameters:
     *             username, password, url, connectionFactory, destination, headerNames, headerValues, payload, connection
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        new Publisher(args).publish();
    }

    /**
     * Default constructor, the parameters for the message then passed into the
     * {@link ikube.jms.Publisher#publish(String, String, String, String, String, String, String, String, String)} method.
     */
    public Publisher() {
    }

    /**
     * The constructor that will take the runtime properties from the command line.
     *
     * @param args the properties and arguments to be used to connect to the provider and the message payload
     * @throws CmdLineException
     */
    public Publisher(final String[] args) throws CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);
    }

    /**
     * Publishes a message to a destination using the properties that are parsed on the command line.
     *
     * @throws JMSException
     * @throws NamingException
     */
    public void publish() throws JMSException, NamingException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        publish(username, password, url, connectionFactory, destination, headerNames, headerValues, payload, connection);
    }

    /**
     * Publishes a message to either a queue or a topic.
     *
     * @param userid            the userid to connect to the connection factory
     * @param password          the password for the connection factory, for example
     * @param url               the url string to the provider, for example 't3://be-qa-cs-18.clear2pay.com:8001'
     * @param connectionFactory the name of the connection factory in JNDI, for example 'jms/QCF'
     * @param destination       the destination name, for example my-queue, or 'jms/InterchangeloaderQ'
     * @param headerNames       header keys for the message, for example 'BankName'
     * @param headerValues      header values for the message, for example 'Credit Suisse'
     * @param payload           the message body, typically text or xml
     * @param connectionType    the connection type, i.e. Weblogic, MQ etc, for example 'ikube.jms.connection.Weblogic'
     * @throws NamingException
     * @throws JMSException
     */
    public void publish(
            final String userid,
            final String password,
            final String url,
            final String connectionFactory,
            final String destination,
            final String headerNames,
            final String headerValues,
            final String payload,
            final String connectionType)
            throws NamingException, JMSException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        IConnector connector = (IConnector) Class.forName(connectionType).newInstance();
        JmsTemplate jmsTemplate = connector.connect(userid, password, url, connectionFactory, destination);
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(final Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(payload);
                String[] headers = StringUtils.split(headerNames, ",;:|");
                String[] values = StringUtils.split(headerValues, ",;:|");
                for (int i = 0; i < headers.length; i++) {
                    String header = headers[i];
                    textMessage.setObjectProperty(header, values[i]);
                }
                return textMessage;
            }
        };
        jmsTemplate.send(messageCreator);
    }

}