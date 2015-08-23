package ikube.experimental.publish;

import ikube.experimental.listener.IndexWriterEvent;
import org.apache.lucene.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * This class publishes the processing data to the dashboard.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
public class IndexWriterPublisher extends AbstractPublish<IndexWriterEvent> {

    @Override
    public void notify(final IndexWriterEvent event) {
        logger.info("Event : " + event.hashCode());
        String apiKey = "3hBgqJHgsdADILee9gmw3rgmT91tI28Z";
        List<Map<Object, Object>> data = event.getData();
        if (data != null) {
            String streamKey = "GNNTIPuG";
            push(apiKey, streamKey, data.size());
            logger.debug("Event : {}", event.hashCode());
        }
        List<Document> documents = event.getDocuments();
        if (documents != null) {
            String streamKey = "f61d2199a4";
            push(apiKey, streamKey, documents.size());
            logger.debug("Event : {}", event.hashCode());
        }
    }

}