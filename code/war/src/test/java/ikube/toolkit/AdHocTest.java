package ikube.toolkit;

import ikube.BaseTest;
import ikube.IConstants;
import ikube.action.Open;
import ikube.action.index.IndexManager;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.search.SearchComplex;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Ignore
public class AdHocTest extends BaseTest {

    private Analyzer analyzer;
    private IndexContext<?> indexContext;

    @Before
    public void before() throws Exception {
        ThreadUtilities.initialize();
        indexContext = new IndexContext<>();
    }

    @After
    public void after() throws IOException {
        if (indexContext.getMultiSearcher() != null) {
            indexContext.getMultiSearcher().getIndexReader().close();
        }
    }

    @Test
    public void doSearch() throws Exception {
        initialize();
        printIndex(indexContext.getMultiSearcher().getIndexReader(), 3);

        SearchComplex searchSpatial = new SearchComplex(indexContext.getMultiSearcher());

        searchSpatial.setFirstResult(0);
        searchSpatial.setMaxResults(10);
        searchSpatial.setFragment(Boolean.TRUE);

        searchSpatial.setSearchStrings(IConstants.NEGATIVE + "~");
        searchSpatial.setSearchFields(IConstants.CLASSIFICATION);
        searchSpatial.setOccurrenceFields(IConstants.SHOULD);
        searchSpatial.setTypeFields(IConstants.STRING);

        ArrayList<HashMap<String, String>> results = searchSpatial.execute();
        printResults(results);
    }

    @Test
    public void doRawSearch() throws Exception {
        doRawSearch("/home/indexes/twitter/1390050382699/192.168.1.8");
    }

    @Test
    public void doRawTest() throws Exception {
        initialize();
        long time = System.currentTimeMillis();
        String ip = UriUtilities.getIp();
        // indexContext.setAnalyzer(new StandardAnalyzer(IConstants.LUCENE_VERSION));
        analyzer = new StemmingAnalyzer();
        ((StemmingAnalyzer) analyzer).setUseStopWords(Boolean.FALSE);
        indexContext.setAnalyzer(analyzer);
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, time, ip);

        addDocument(indexWriter, IConstants.POSITIVE);
        addDocument(indexWriter, IConstants.NEGATIVE);

        IndexManager.closeIndexWriter(indexWriter);

        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
        logger.info("Latest index directory : " + latestIndexDirectory);

        doRawSearch(latestIndexDirectory.getAbsolutePath() + IConstants.SEP + ip);
    }

    private void doRawSearch(final String indexDirectory) throws Exception {
        File file = new File(indexDirectory);
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(file));
        printIndex(indexReader, 10);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        QueryParser queryParser = new QueryParser(IConstants.LUCENE_VERSION, IConstants.CLASSIFICATION, analyzer);
        Query query = queryParser.parse(IConstants.NEGATIVE);
        TopDocs topDocs = indexSearcher.search(query, 10);
        logger.info("Hits : " + topDocs.totalHits);
        indexReader.close();
    }

    private void addDocument(final IndexWriter indexWriter, final String contents) throws IOException {
        Document document = new Document();
        Indexable indexable = getIndexable();
        IndexManager.addStringField(IConstants.CLASSIFICATION, contents, indexable, document);
        indexWriter.addDocument(document);
    }

    private Indexable<?> getIndexable() {
        Indexable<?> indexable = new Indexable<Object>() {
        };
        indexable.setStored(Boolean.TRUE);

        indexable.setAnalyzed(Boolean.TRUE);
        indexable.setOmitNorms(Boolean.TRUE);
        indexable.setTokenized(Boolean.TRUE);

        indexable.setVectored(Boolean.FALSE);
        return indexable;
    }

    private void initialize() throws Exception {
        indexContext.setIndexName("twitter");
        indexContext.setIndexDirectoryPath("/home/indexes");
        indexContext.setAnalyzer(new StemmingAnalyzer());
        indexContext.setBufferedDocs(10000);
        indexContext.setBufferSize(1024);
        indexContext.setBatchSize(10000);
        indexContext.setCompoundFile(Boolean.TRUE);
        indexContext.setMaxFieldLength(10000);
        indexContext.setMaxReadLength(Integer.MAX_VALUE);
        indexContext.setMergeFactor(10000);
        indexContext.setMaxExceptions(1000);

        // new Optimizer().execute(indexContext);
        new Open().execute(indexContext);
    }

    private void printResults(final ArrayList<HashMap<String, String>> results) {
        for (final HashMap<String, String> result : results) {
            logger.info("Result : ");
            for (final Map.Entry<String, String> mapEntry : result.entrySet()) {
                logger.info("       : " + mapEntry.getKey() + "-" + mapEntry.getValue());
            }
        }
    }

}