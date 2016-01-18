package ikube.jms;

import ikube.IConstants;
import ikube.jms.connect.IConnector;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * This is a simple JMS client that will publish a message to either a topic or a queue. Parameters
 * can be passed to the publisher.
 * <p/>
 * <pre>
 *     java -jar ikube-tool-5.3.0.jar ikube.jms.Publisher -u admin -p password -u t3://be-qa-cs-18.clear2pay.com:8001 -cf
 *          jms/QCF -d jms/InterchangeLoaderQ -pl Hello-World -ct ikube.jms.connect.Weblogic -i 10 -f messages
 *     java -jar ikube-tool-5.3.0.jar ikube.jms.Publisher -u admin -p password -u iiop://192.168.1.102:2809 -cf
 *          cell/nodes/app1/servers/OPFClusterApp1/jms/QCF -d cell/nodes/app1/servers/OPFClusterApp1/jms/InterchangeLoaderQ -pl
 *          Hello-World -ct ikube.jms.connect.WebSphereMQ -i 10 -f messages
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class Publisher {

    private static final int MAX_THREADS = 100;

    @SuppressWarnings("UnusedDeclaration")
    private Logger logger = LoggerFactory.getLogger(Publisher.class);

    @Option(name = "-u")
    private String username;
    @Option(name = "-p")
    private String password;
    @Option(name = "-url")
    private String url;
    @Option(name = "-cf")
    private String connectionFactoryName;
    @Option(name = "-d")
    private String destinationName;
    @Option(name = "-ct")
    private String connectorType;

    @Option(name = "-h")
    private String headerNames;
    @Option(name = "-v")
    private String headerValues;
    @Option(name = "-pl")
    private String payload;
    @Option(name = "-f")
    private String folder;
    @Option(name = "-i")
    private int iterations;

    /**
     * The main that will be called from the {@link ikube.Ikube} class.
     *
     * @param args arguments are very specific, and they are all mandatory except for the header names and values. So
     *             to enumerate the required parameters:
     *             username, password, url, connectionFactoryName, destinationName, headerNames, headerValues, payload, connectorType
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        new Publisher(args).publish();
        System.exit(0);
    }

    /**
     * Default constructor, the parameters for the message then passed into the
     * {@link ikube.jms.Publisher#publish(String, String, String, String, String, String, String, String, String, int, String)} method.
     */
    public Publisher() {
        THREAD.initialize();
    }

    /**
     * The constructor that will take the runtime properties from the command line.
     *
     * @param args the properties and arguments to be used to connect to the provider and the message payload
     * @throws CmdLineException
     */
    public Publisher(final String[] args) throws CmdLineException {
        this();
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);
    }

    /**
     * Publishes a message to a destinationName using the properties that are parsed on the command line.
     *
     * @throws JMSException
     * @throws NamingException
     */
    public void publish() throws Exception {
        publish(username, password, url, connectionFactoryName, destinationName, headerNames, headerValues, payload, connectorType, iterations, folder);
    }

    /**
     * Publishes a message to either a queue or a topic.
     *
     * @param userid                the userid to connect to the connectorType factory
     * @param password              the password for the connectorType factory, for example
     * @param url                   the url string to the provider, for example 't3://be-qa-cs-18.clear2pay.com:8001'
     * @param connectionFactoryName the name of the connectorType factory in JNDI, for example 'jms/QCF'
     * @param destinationName       the destinationName name, for example my-queue, or 'jms/InterchangeloaderQ'
     * @param headerNames           header keys for the message, for example 'BankName'
     * @param headerValues          header values for the message, for example 'Credit Suisse'
     * @param payload               the message body, typically text or xml
     * @param connectorType         the connectorType type, i.e. Weblogic, MQ etc, for example 'ikube.jms.connectorType.Weblogic'
     * @throws NamingException
     * @throws JMSException
     */
    public void publish(
            final String userid,
            final String password,
            final String url,
            final String connectionFactoryName,
            final String destinationName,
            final String headerNames,
            final String headerValues,
            final String payload,
            final String connectorType,
            final int iterations,
            final String folderName)
            throws Exception {

        String[] headers = new String[0];
        String[] values = new String[0];
        if (StringUtils.isNotEmpty(headerNames) && StringUtils.isNotEmpty(headerValues)) {
            headers = StringUtils.split(headerNames, ",;:|");
            values = StringUtils.split(headerValues, ",;:|");
        }
        logger.info("Headers and values not being populated : names : {}, values : {}", headerNames, headerValues);

        IConnector connector = (IConnector) Class.forName(connectorType).newInstance();
        JmsTemplate jmsTemplate = connector.connect(userid, password, url, connectionFactoryName, destinationName);

        logger.info("Payload : " + payload + ", folder name : " + folderName);
        if (StringUtils.isNotEmpty(payload)) {
            publishText(jmsTemplate, payload, headers, values, iterations);
        } else if (StringUtils.isNotEmpty(folderName)) {
            publishFiles(jmsTemplate, folderName, headers, values, iterations);
        } else {
            throw new RuntimeException("Either the payload or the folder for the files needs to be specified : ");
        }
    }

    void publishText(final JmsTemplate jmsTemplate, final String payload, final String[] headers, final String[] values, final int iterations) {
        String[] array = new String[Math.min(iterations, MAX_THREADS)];
        Arrays.fill(array, payload);
        List<String> payloads = Arrays.asList(array);
        for (int i = 0; i < Math.max(1, (iterations / payloads.size())); i++) {
            publish(jmsTemplate, payloads, headers, values);
        }
    }

    void publishFiles(final JmsTemplate jmsTemplate, final String folderName, final String[] headers, final String[] values, final int iterations) {
        List<File> files = FILE.findFilesRecursively(new File(folderName), new ArrayList<File>(), ".xml");
        if (!files.isEmpty()) {
            Collections.sort(files);
            List<String> payloads = new ArrayList<>();
            for (final File file : files) {
                String payload = FILE.getContents(file, IConstants.ENCODING);
                payloads.add(payload);
            }
            for (int i = 0; i < Math.max(1, (iterations / payloads.size())); i++) {
                publish(jmsTemplate, payloads, headers, values);
            }
        } else {
            logger.warn("Message folder empty : " + folder);
        }
    }

    void publish(final JmsTemplate jmsTemplate, final List<String> payloads, final String[] headers, final String[] values) {
        final String jobName = this.getClass().getSimpleName();
        List<Future<Object>> futures = new ArrayList<>();
        for (final String payload : payloads) {
            @SuppressWarnings("unchecked")
            Future<Object> future = (Future<Object>) THREAD.submit(jobName, new Runnable() {
                public void run() {
                    logger.debug("Sending message : {}, {}, {} ", new Object[]{payload, headers, values});
                    MessageCreator messageCreator = getMessageCreator(payload, headers, values);
                    jmsTemplate.send(messageCreator);
                }
            });
            futures.add(future);
            if (futures.size() >= MAX_THREADS) {
                THREAD.waitForFutures(futures, 60);
            }
        }
        THREAD.waitForFutures(futures, 60);
        THREAD.destroy(jobName);
    }

    MessageCreator getMessageCreator(final String payload, final String[] headers, final String[] values) {
        return new MessageCreator() {
            @Override
            public Message createMessage(final Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(payload);
                for (int i = 0; i < headers.length; i++) {
                    textMessage.setObjectProperty(headers[i], values[i]);
                }
                return textMessage;
            }
        };
    }

}