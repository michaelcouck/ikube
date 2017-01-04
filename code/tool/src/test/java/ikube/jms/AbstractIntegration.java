package ikube.jms;

import ikube.AbstractTest;
import ikube.jms.connect.WebSphereConnector;
import ikube.toolkit.THREAD;
import org.apache.commons.lang.RandomStringUtils;
import org.dom4j.Element;
import org.mockito.Spy;

public abstract class AbstractIntegration extends AbstractTest {

    protected String userid = "userid";
    protected String password = "password";
    protected String url = "iiop://url-to-websphere:2809";
    protected String connectionFactory = "connectin/factory/path";
    protected String destinationPrefix = "destination/path";
    protected String queueSuffix = "queue_Suffix";
    protected String connectorType = WebSphereConnector.class.getName();

    protected String headerNames = "headers";

    @Spy
    protected Publisher publisher;

    protected String[] queues = new String[]{
    };

    protected String queue = queues[20];

    protected void publishRemoteWas(final String queue, final String headerValues, final String payload) throws Exception {
        publisher.publish(
                userid, // userid
                password, // password
                url, // url
                connectionFactory, // connection factory
                destinationPrefix + queue, // destination name
                headerNames, // header names
                headerValues, // header values
                payload,
                connectorType, 1, null);
    }

    protected void randomNumber(final Element element) {
        String numbers = "1234567890";
        String text = element.getText();
        int lastIndeOfForwardSlash = text.lastIndexOf('/') + 1;
        String number = RandomStringUtils.random(6, numbers);
        element.setText(text.substring(0, lastIndeOfForwardSlash) + number);
    }

    protected void randomString(final Element element) {
        String alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String text = element.getText();
        String randomText = RandomStringUtils.random(text.length(), alphaNumeric);
        element.setText(randomText);
    }

    protected void sleep(final long start) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - start;
        long period = 60000;
        if (duration < period) {
            long sleep = period - duration;
            logger.info("Sleeping for : " + sleep);
            THREAD.sleep(sleep);
        }
    }

}
