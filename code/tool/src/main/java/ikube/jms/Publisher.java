package ikube.jms;

import ikube.toolkit.FILE;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;

/**
 * This is a simple JMS client that will publish a message to either a topic or a queue. Parameters
 * can be passed to the publisher, and optionally a file can be used for the message body, in this way
 * a trivially large message can be published to the queue.
 * <p/>
 * The connection factory details must be configured in the jndi.properties file, that must be in the
 * same directory that the jvm is started in. An example of the jndi file is available.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class Publisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);

    /**
     * The main that will be called from the {@link ikube.Ikube} class.
     *
     * @param args arguments are very
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        new Publisher().publish(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
    }

    /**
     * Publishes a message to either a queue or a topic. There can be parameters added to
     * the message and a file specified as the input for the test message. An example of input
     * would be:
     *
     * <pre>
     *     java -jar ikube-tool-5.2.0.jar ikube.jms.Publisher userid password ConnectionFactory MyTopic jms-client.xml property value
     * </pre>
     *
     * @param userid                the userid to connect to the connection factory
     * @param password              the password for the connection factory
     * @param connectionFactoryName the name of the connection factory in JNDI
     * @param destinationName       the destination name, for example my-topic
     * @param fileName              the name of the file that will be used as the input for the message body
     * @param parameterNames        parameter names
     * @param parameterValues       parameter values
     * @throws NamingException
     * @throws JMSException
     */
    public void publish(
            final String userid,
            final String password,
            final String connectionFactoryName,
            final String destinationName,
            final String fileName,
            final String parameterNames,
            final String parameterValues)
            throws NamingException, JMSException {

        String[] parameterNamesArray = StringUtils.split(parameterNames, ",;:|");
        String[] parameterValuesArray = StringUtils.split(parameterValues, ",;:|");

        String contents = "";
        File file = FILE.findFileRecursively(new File("."), fileName);
        if (file != null) {
            contents = FILE.getContent(file);
        } else {
            LOGGER.warn("No file found for message body : " + fileName);
        }

        Context context = new InitialContext();
        ConnectionFactory factory = (ConnectionFactory) context.lookup(connectionFactoryName);
        Connection connection = factory.createConnection(userid, password);
        connection.start();

        Destination destination = (Destination) context.lookup(destinationName);
        Session session = connection.createSession(Boolean.FALSE, TopicSession.AUTO_ACKNOWLEDGE);

        MessageProducer producer = session.createProducer(destination);
        Message message = session.createTextMessage(contents);

        for (int i = 0; i < parameterNamesArray.length; i++) {
            message.setStringProperty(parameterNamesArray[i], parameterValuesArray[i]);
        }

        LOGGER.info("Sending message : " + message);

        producer.send(destination, message);
    }

}