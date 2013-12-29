package ikube;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.search.spelling.SpellingChecker;
import ikube.toolkit.Logging;
import ikube.toolkit.UriUtilities;
import mockit.Deencapsulation;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
        SpellingChecker checkerExt = new SpellingChecker();
        Deencapsulation.setField(checkerExt, "languageWordListsDirectory", "languages");
        Deencapsulation.setField(checkerExt, "spellingIndexDirectoryPath", "./spellingIndex");
        try {
            checkerExt.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (int i = 0; i < names.length && i < values.length; i++) {
            NameValuePair nameValuePair = new NameValuePair(names[i], values[i]);
            nameValuePairs.add(nameValuePair);
        }
        return nameValuePairs.toArray(new NameValuePair[nameValuePairs.size()]);
    }

    /**
     * This method creates an index using the index path in the context, the time and the ip and returns the latest index directory, i.e.
     * the index that has just been created. Note that if there are still cascading mocks from JMockit, the index writer sill not create the
     * index! So you have to tear down all mocks prior to using this method.
     *
     * @param indexContext the index context to use for the path to the index
     * @param strings      the data that must be in the index
     * @return the latest index directory, i.e. the one that has just been created
     */
    protected File createIndex(final IndexContext<?> indexContext, final String... strings) {
        IndexWriter indexWriter = null;
        String ip = null;
        try {
            ip = UriUtilities.getIp();
            indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
            Document document = new Document();
            IndexManager.addStringField(IConstants.CONTENTS, "Michael Couck", indexContext, document);
            indexWriter.addDocument(document);
            for (String string : strings) {
                document = new Document();
                IndexManager.addStringField(IConstants.CONTENTS, string, indexContext, document);
                indexWriter.addDocument(document);
            }
        } catch (Exception e) {
            logger.error("Exception creating the index : ", e);
        } finally {
            IndexManager.closeIndexWriter(indexWriter);
        }
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
        return latestIndexDirectory;
    }

    protected void printIndex(final IndexSearcher multiSearcher) throws Exception {
        IndexReader indexReader = multiSearcher.getIndexReader();
        printIndex(indexReader);
    }

    /**
     * This method will just print the data in the index reader.L
     *
     * @param indexReader the reader to print the documents for
     * @throws Exception
     */
    protected void printIndex(final IndexReader indexReader) throws Exception {
        printIndex(indexReader, indexReader.numDocs());
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
                logger.info("        : " + fieldName + ", " + fieldLength + ", " + fieldValue);
            }
        }
    }

}
