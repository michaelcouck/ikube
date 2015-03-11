package ikube.jms;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * This is an example of a connection to an ActiveMQ message broker using remote JNDI.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-03-2015
 */
public class JmsProducerConsumer {

    public static void main(String args[]) throws Exception {
        // create a new initial context, which loads from jndi.properties file
        Context context = new InitialContext();
        // lookup the connection factory
        ConnectionFactory factory = (TopicConnectionFactory) context.lookup("ConnectionFactory");
        // create a new TopicConnection for pub/sub messaging
        Connection connection = factory.createConnection();
        connection.start();

        // lookup an existing topic
        final Destination destination = (Destination) context.lookup("MyTopic");
        // create a new TopicSession for the client
        final Session session = connection.createSession(false, TopicSession.AUTO_ACKNOWLEDGE);

        new Thread(new Runnable() {
            public void run() {
                int iterations = 10;
                do {
                    MessageConsumer consumer = null;
                    try {
                        consumer = session.createConsumer(destination);
                        Message message = consumer.receive();
                        System.out.println("Received message : " + message);
                        Thread.sleep(1000);
                    } catch (final JMSException | InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (consumer != null) {
                                consumer.close();
                            }
                        } catch (final JMSException e) {
                            e.printStackTrace();
                        }
                    }
                } while (iterations-- > 0);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                int iterations = 10;
                do {
                    MessageProducer producer = null;
                    try {
                        producer = session.createProducer(destination);
                        Message message = session.createTextMessage("Producer");
                        System.out.println("Publishing message : " + message + " : " + destination);
                        producer.send(destination, message);
                        Thread.sleep(1000);
                    } catch (final JMSException | InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (producer != null) {
                            try {
                                producer.close();
                            } catch (final JMSException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } while (iterations-- > 0);
            }
        }).start();
    }
}