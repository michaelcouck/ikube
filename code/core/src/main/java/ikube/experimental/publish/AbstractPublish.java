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

    class Point {

        private String latitude;

        private String longitude;

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(final String longitude) {
            this.longitude = longitude;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(final String latitude) {
            this.latitude = latitude;
        }

    }

    class Event {

        private String accessKey;
        private String streamName;
        private Object point;

        public Object getPoint() {
            return point;
        }

        public void setPoint(final Object point) {
            this.point = point;
        }

        public String getStreamName() {
            return streamName;
        }

        public void setStreamName(final String streamName) {
            this.streamName = streamName;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(final String accessKey) {
            this.accessKey = accessKey;
        }

    }


    Logger logger = LoggerFactory.getLogger(this.getClass());

    void push(final String apiKey, final String stream, final double datum) {
        Event event = new Event();
        event.setAccessKey(apiKey);
        event.setStreamName(stream);
        event.setPoint(Double.toString(datum));

        REST.doPost("https://www.leftronic.com/customSend/", event, String.class);
    }

    void push(final String apiKey, final String stream, final double latitude, final double longitude) {
        Event event = new Event();
        event.setAccessKey(apiKey);
        event.setStreamName(stream);

        Point point = new Point();
        point.setLatitude(String.valueOf(latitude));
        point.setLongitude(String.valueOf(longitude));

        event.setPoint(Arrays.asList(point));

        REST.doPost("https://www.leftronic.com/customSend/", event, String.class);
    }

}
