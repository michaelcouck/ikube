package ikube.experimental;

import com.google.gson.Gson;
import ikube.experimental.listener.IConsumer;
import ikube.experimental.listener.IndexWriterEvent;
import ikube.experimental.publisher.Event;
import ikube.experimental.publisher.Point;
import ikube.toolkit.REST;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class publishes the processing data to the dashboard.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
public class Publisher implements IConsumer<IndexWriterEvent> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notify(final IndexWriterEvent indexWriterEvent) {
        logger.info("Event : " + indexWriterEvent.hashCode());
        String apiKey = "3hBgqJHgsdADILee9gmw3rgmT91tI28Z";
        List<Map<Object, Object>> data = indexWriterEvent.getData();
        if (data != null) {
            String streamKey = "GNNTIPuG";
            push(apiKey, streamKey, data.size());
        }
        List<Document> documents = indexWriterEvent.getDocuments();
        if (documents != null) {
            String streamKey = "f61d2199a4";
            push(apiKey, streamKey, documents.size());
        }

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        String streamKey = "W6rv4Ee2";
        double systemLoad = (operatingSystemMXBean.getSystemLoadAverage() * 100d) / operatingSystemMXBean.getAvailableProcessors();
        push(apiKey, streamKey, systemLoad);

        streamKey = "9kAZdmOd";
        double latitude = 90d * new Random().nextDouble();
        double longitude = 180d * new Random().nextDouble();
        if (System.currentTimeMillis() % 2 == 0) {
            longitude = -longitude;
        }
        push(apiKey, streamKey, latitude, longitude);
    }

    private void push(final String apiKey, final String stream, final double latitude, final double longitude) {
        Event event = new Event();
        event.setAccessKey(apiKey);
        event.setStreamName(stream);

        Point point = new Point();
        point.setLatitude(String.valueOf(latitude));
        point.setLongitude(String.valueOf(longitude));

        event.setPoint(Arrays.asList(point));

        logger.info(new Gson().toJson(event) + ", " + event.hashCode() + ", " + this.hashCode());

        REST.doPost("https://www.leftronic.com/customSend/", event, String.class);
    }

    private void push(final String apiKey, final String stream, final double datum) {
        Event event = new Event();
        event.setAccessKey(apiKey);
        event.setStreamName(stream);
        event.setPoint(Double.toString(datum));

        logger.info(new Gson().toJson(event) + ", " + event.hashCode() + ", " + this.hashCode());

        REST.doPost("https://www.leftronic.com/customSend/", event, String.class);
    }

}