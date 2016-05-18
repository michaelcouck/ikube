package ikube.jms.connect;

import ikube.toolkit.THREAD;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Message;

public class Consumer {

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
        new Consumer(args).consume();
        System.exit(0);
    }

    public Consumer() {
        THREAD.initialize();
    }

    public Consumer(final String[] args) throws CmdLineException {
        this();
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(140);
        parser.parseArgument(args);
    }

    public void consume() throws Exception {
        consume(username, password, url, connectionFactoryName, destinationName, queueName, connectorType);
    }

    public void consume(
            final String username,
            final String password,
            final String url,
            final String connectionFactory,
            final String destinationName,
            final String queueName,
            final String connectorType)
            throws Exception {
        logger.info("Factory : " + connectionFactory + ", " + destinationName + ", " + queueName);
        IConnector connector = (IConnector) Class.forName(connectorType).newInstance();
        JmsTemplate jmsTemplate = connector.connect(username, password, url, connectionFactory, destinationName);
        jmsTemplate.setReceiveTimeout(-1);
        Message message = jmsTemplate.receive(queueName);
        while (message != null) {
            logger.info(null, message);
            message = jmsTemplate.receive(queueName);
        }
    }

}
