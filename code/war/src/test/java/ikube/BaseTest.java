package ikube;

import ikube.ikube.mock.SpellingCheckerMock;
import ikube.toolkit.Logging;
import mockit.Mockit;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base test class for the unit tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21.11.12
 */
@SuppressWarnings("deprecation")
public abstract class BaseTest {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String SERVICE = "/service";

    protected static int SERVER_PORT = 8080;
    protected static String LOCALHOST = "localhost";
    protected static String REST_PASSWORD = "user";
    protected static String REST_USER_NAME = "user";
    /**
     * This client({@link HttpClient}) is for the web services.
     */
    protected static HttpClient HTTP_CLIENT = new HttpClient();

    static {
        Logging.configure();
        Mockit.setUpMocks(SpellingCheckerMock.class);
    }

    /**
     * This method will build an array of name value pairs that can be used in the HttpClient to parameterize the request to resources and
     * pages in fact.
     *
     * @param names  the names of the parameters
     * @param values the values to be assigned to the parameters
     * @return the array of name value pairs for the request
     */
    protected static NameValuePair[] getNameValuePairs(final String[] names, final String[] values) {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (int i = 0; i < names.length && i < values.length; i++) {
            NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
            nameValuePairs.add(nameValuePair);
        }
        return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
    }

    protected void printIndex(final IndexSearcher multiSearcher) throws Exception {
        printIndex(multiSearcher.getIndexReader());
    }

    /**
     * This method will just print the data in the index reader.L
     *
     * @param indexReader the reader to print the documents for
     * @throws Exception
     */
    protected void printIndex(final IndexReader indexReader) throws Exception {
        printIndex(indexReader, 10);
    }

    /**
     * This method will just print the data in the index reader.L
     *
     * @param indexReader the reader to print the documents for
     * @param numDocs     the maximum number of documents to print from this index reader
     * @throws Exception
     */
    protected void printIndex(final IndexReader indexReader, final int numDocs) throws Exception {
        logger.info("Num docs : " + indexReader.numDocs());
        for (int i = 0; i < indexReader.numDocs() && i < numDocs; i++) {
            Document document = indexReader.document(i);
            logger.info("Document : " + i);
            List<IndexableField> fields = document.getFields();
            for (IndexableField fieldable : fields) {
                String fieldName = fieldable.name();
                String fieldValue = fieldable.stringValue();
                int fieldLength = fieldValue != null ? fieldValue.length() : 0;
                logger.info("        : " + fieldName + ", " + fieldLength + ", " + fieldValue + ", " + fieldable);
            }
        }
    }

}
