package ikube;

import ikube.analytics.weka.WekaClusterer;
import ikube.mock.SpellingCheckerMock;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import mockit.Mockit;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.clusterers.SimpleKMeans;

import java.io.File;
import java.util.List;

/**
 * This is the base test class for the unit tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2012
 */
@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractTest {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String SERVICE = "/service";

    protected static int SERVER_PORT = 9090;
    protected static String LOCALHOST = "localhost";
    protected static String REST_PASSWORD = "user";
    protected static String REST_USER_NAME = "user";
    /**
     * This client({@link org.apache.http.client.HttpClient}) is for the web services.
     */
    protected static HttpClient HTTP_CLIENT = new DefaultHttpClient();

    static {
        Logging.configure();
        Mockit.setUpMocks(SpellingCheckerMock.class);
    }

    @SuppressWarnings("UnusedDeclaration")
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
        logger.error("Num docs : " + indexReader.numDocs());
        for (int i = 0; i < indexReader.numDocs() && i < numDocs; i++) {
            Document document = indexReader.document(i);
            logger.error("Document : " + i);
            List<IndexableField> fields = document.getFields();
            for (final IndexableField fieldable : fields) {
                String fieldName = fieldable.name();
                String fieldValue = fieldable.stringValue();
                int fieldLength = fieldValue != null ? fieldValue.length() : 0;
                if (fieldName.equalsIgnoreCase(IConstants.CLASSIFICATION)) {
                    logger.error("        : " + fieldName + ", " + fieldLength + ", " + fieldValue + ", " + fieldable);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Context getContext(final String fileName, final String name) throws Exception {
        File trainingDataFile = FileUtilities.findFileRecursively(new File("."), fileName);
        String trainingData = FileUtilities.getContent(trainingDataFile);

        Context context = new Context();
        context.setName(name);
        context.setAnalyzer(WekaClusterer.class.getName());
        context.setAlgorithms(SimpleKMeans.class.getName());
        context.setOptions("-N", "6");

        context.setTrainingDatas(trainingData);
        context.setMaxTrainings(Integer.MAX_VALUE);

        return context;
    }

    protected Analysis getAnalysis(final String context, final String input) {
        Analysis<String, double[]> analysis = new Analysis<>();
        analysis.setContext(context);
        analysis.setInput(input);
        return analysis;
    }

}
