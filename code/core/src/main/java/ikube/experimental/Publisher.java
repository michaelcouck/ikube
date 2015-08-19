package ikube.experimental;

import com.google.gson.Gson;
import ikube.experimental.listener.IListener;
import ikube.experimental.listener.IndexWriterEvent;
import ikube.experimental.publisher.Data;
import ikube.experimental.publisher.Push;
import ikube.toolkit.REST;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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
public class Publisher implements IListener<IndexWriterEvent> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * <pre>
     *      {
     *      "data":  [
     *          {
     *              "Date":  "20130320",
     *              "Users":  "1"
     *          }
     *      ],
     *      "onduplicate":  {
     *          "Users":  "replace"
     *      },
     *      "color":  {
     *          "Users":  "#52ff7f"
     *      },
     *          "type":  {
     *              "Users":  "line"
     *          }
     *      }
     * </pre>
     */
    @Override
    public void notify(final IndexWriterEvent event) {
        List<Map<Object, Object>> data = event.getData();
        if (data != null) {
            logger.info("Data push : ", event.getContext().getName());
            Push push = getPush(System.currentTimeMillis(), data.size());
            REST.doPost("https://app.cyfe.com/api/push/55d45caef38b02341784981487634", push, Void.class);
        }
        List<Document> documents = event.getDocuments();
        if (documents != null) {
            logger.info("Document push : ", event.getContext().getName());
            Push push = getPush(System.currentTimeMillis(), documents.size());
            REST.doPost("https://app.cyfe.com/api/push/55d46b291ea8b9000300881487760", push, Void.class);
        }
    }

    private Push getPush(final long time, final int size) {
        // Post the number of rows/documents popped into the grid
        Push push = new Push();
        Data data = new Data();
        push.setData(Arrays.asList(data));
        data.setPoint(Long.toString(time));
        data.setEvent(Integer.toString(size));
        logger.info(new Gson().toJson(push));
        return push;
    }

}