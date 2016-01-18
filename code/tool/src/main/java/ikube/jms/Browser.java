package ikube.jms;

import ikube.jms.connect.IConnector;
import ikube.toolkit.THREAD;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import java.util.Enumeration;

public class Browser {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

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
    @Option(name = "-q")
    private String queueName;
    @Option(name = "-ct")
    private String connectorType;

    public static void main(final String[] args) throws Exception {
        new Browser(args).browse();
        System.exit(0);
    }

    public Browser() {
        THREAD.initialize();
    }

    public Browser(final String[] args) throws CmdLineException {
        this();
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);
    }

    public void browse() throws Exception {
        browse(username, password, url, connectionFactoryName, destinationName, queueName, connectorType);
    }

    public void browse(
            final String username,
            final String password,
            final String url,
            final String connectionFactory,
            final String destinationName,
            final String queueName,
            final String connectorType)
            throws Exception {
        IConnector connector = (IConnector) Class.forName(connectorType).newInstance();
        JmsTemplate jmsTemplate = connector.connect(username, password, url, connectionFactory, destinationName);
        jmsTemplate.browse(queueName, new BrowserCallback<Void>() {
            @Override
            public Void doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                Enumeration enumeration = browser.getEnumeration();
                if (enumeration.hasMoreElements()) {
                    while (enumeration.hasMoreElements()) {
                        Object message = enumeration.nextElement();
                        logger.info("Message : " + message);
                    }
                } else {
                    logger.info("No messages on destination : " + destinationName + ", queue : " + queueName);
                }
                return null;
            }
        });
    }

}
