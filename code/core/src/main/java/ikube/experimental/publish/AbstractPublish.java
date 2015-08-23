package ikube.experimental.publish;


import ikube.experimental.listener.IConsumer;
import ikube.experimental.listener.IEvent;
import ikube.toolkit.REST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This class publishes the processing data to the dashboard.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
public abstract class AbstractPublish<T extends IEvent<?, ?>> implements IConsumer<T> {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    void push(final String apiKey, final String stream, final double datum) {
        PublishingEvent event = new PublishingEvent();
        event.setAccessKey(apiKey);
        event.setStreamName(stream);
        event.setPoint(Double.toString(datum));

        REST.doPost("https://www.leftronic.com/customSend/", event, String.class);
    }

    void push(final String apiKey, final String stream, final double latitude, final double longitude) {
        PublishingEvent event = new PublishingEvent();
        event.setAccessKey(apiKey);
        event.setStreamName(stream);

        Point point = new Point();
        point.setLatitude(String.valueOf(latitude));
        point.setLongitude(String.valueOf(longitude));

        event.setPoint(Arrays.asList(point));

        REST.doPost("https://www.leftronic.com/customSend/", event, String.class);
    }

}
