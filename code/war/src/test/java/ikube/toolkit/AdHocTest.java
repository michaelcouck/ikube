package ikube.toolkit;

import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.model.IndexContext;
import ikube.search.SearchComplex;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static ikube.IConstants.*;

@Ignore
public class AdHocTest extends AbstractTest {

    @Test
    public void adHoc() throws Exception {
        IndexContext indexContext = new IndexContext();
        indexContext.setBufferedDocs(100);
        indexContext.setBufferSize(256);
        indexContext.setCompoundFile(Boolean.TRUE);
        indexContext.setIndexDirectoryPath("./indexes");
        indexContext.setIndexName("range-query");
        indexContext.setMaxFieldLength(10000);
        indexContext.setMergeFactor(100);

        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), "127.0.0.1");
        addFields(indexWriter);
        indexWriter.commit();
        indexWriter.close();

        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
        Directory directory = FSDirectory.open(new File(latestIndexDirectory, "127.0.0.1"));
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        SearchComplex searchComplex = new SearchComplex(indexSearcher);
        searchComplex.setFirstResult(0);
        searchComplex.setFragment(Boolean.TRUE);
        searchComplex.setMaxResults(1000);
        searchComplex.setSearchStrings("0-5");
        searchComplex.setTypeFields(RANGE);
        searchComplex.setOccurrenceFields(SHOULD);
        searchComplex.setSearchFields("range-field");
        ArrayList<HashMap<String, String>> results = searchComplex.execute();
        logger.error("Results : " + results.size());

        searchComplex.setSearchStrings("0-8");
        results = searchComplex.execute();
        logger.error("Results : " + results.size());
    }

    @Test
    public void twitter() throws Exception {
        File latestIndexDirectory = new File("/tmp/indexes/twitter/1407656295749/192.168.1.8");
        Directory directory = FSDirectory.open(latestIndexDirectory);
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        SearchComplex searchComplex = new SearchComplex(indexSearcher, new StemmingAnalyzer());
        searchComplex.setFirstResult(0);
        searchComplex.setMaxResults(10);
        searchComplex.setFragment(Boolean.TRUE);

        String[] ranges = {"1407567157793-1407570757793",
                "1407570757793-1407574357793",
                "1407574357793-1407577957793",
                "1407577957793-1407581557793",
                "1407581557793-1407585157793",
                "1407585157793-1407588757793",
                "1407588757793-1407592357793",
                "1407592357793-1407595957793",
                "1407595957793-1407599557793",
                "1407599557793-1407603157793",
                "1407603157793-1407606757793",
                "1407606757793-1407610357793",
                "1407610357793-1407613957793",
                "1407613957793-1407617557793",
                "1407617557793-1407621157793",
                "1407621157793-1407624757793",
                "1407624757793-1407628357793",
                "1407628357793-1407631957793",
                "1407631957793-1407635557793",
                "1407635557793-1407639157793",
                "1407639157793-1407642757793",
                "1407642757793-1407646357793",
                "1407646357793-1407649957793",
                "1407649957793-1407653557793"};

        for (final String range : ranges) {
            searchComplex.setSearchStrings(range, "positi*");
            searchComplex.setTypeFields(RANGE, STRING);
            searchComplex.setOccurrenceFields(MUST, MUST);
            searchComplex.setSearchFields("created-at", CLASSIFICATION);

            ArrayList<HashMap<String, String>> results = searchComplex.execute();
            if (results.size() > 1) {
                // logger.error("Results : " + results.get(results.size() - 1).get(TOTAL) + ", range : " + range);
            }
        }

        long startTime = System.currentTimeMillis() - (1000 * 60 * 60 * 6);
        do {
            long oneHourMillis = 1000 * 60;
            startTime += oneHourMillis;
            long endTime = startTime + oneHourMillis;
            String range = startTime + "-" + endTime;

            searchComplex.setSearchStrings("", "", range, "positive");
            searchComplex.setTypeFields(STRING, STRING, RANGE, STRING);
            searchComplex.setOccurrenceFields(MUST, MUST, MUST, MUST);
            searchComplex.setSearchFields("contents", "language-original", "created-at", CLASSIFICATION);

            ArrayList<HashMap<String, String>> results = searchComplex.execute();
            if (results.size() > 1) {
                logger.error("Results : " + results.get(results.size() - 1).get(TOTAL));
            }
        } while (startTime < System.currentTimeMillis());

        // printIndex(indexReader, 10);

        indexSearcher.getIndexReader().close();
    }

    private void addFields(final IndexWriter indexWriter) throws IOException {
        for (int i = 0; i < 10; i++) {
            Document document = new Document();
            IndexManager.addNumericField("range-field", Integer.toString(i), document, Boolean.TRUE, 0.0f);
            indexWriter.addDocument(document);
        }
    }

    @Test
    public void anotherAdHoc() throws Exception {
        // This works, there are more results from with a broadening range
        RAMDirectory idx = new RAMDirectory();
        IndexWriter writer = new IndexWriter(
                idx,
                new IndexWriterConfig(Version.LUCENE_40, new ClassicAnalyzer(Version.LUCENE_40))
        );
        Document document = new Document();
        document.add(new StringField("ticket_number", "t123", Field.Store.YES));
        document.add(new IntField("ticket_id", 1, Field.Store.YES));
        document.add(new StringField("id_s", "234", Field.Store.YES));
        writer.addDocument(document);

        document = new Document();
        document.add(new StringField("ticket_number", "t123", Field.Store.YES));
        document.add(new IntField("ticket_id", 100, Field.Store.YES));
        document.add(new StringField("id_s", "234", Field.Store.YES));
        writer.addDocument(document);

        document = new Document();
        document.add(new StringField("ticket_number", "t123", Field.Store.YES));
        document.add(new IntField("ticket_id", 234, Field.Store.YES));
        document.add(new StringField("id_s", "234", Field.Store.YES));
        writer.addDocument(document);

        writer.commit();

        IndexReader reader = DirectoryReader.open(idx);
        IndexSearcher searcher = new IndexSearcher(reader);

        Query q1 = new TermQuery(new Term("id_s", "234"));
        TopDocs td1 = searcher.search(q1, 1);
        System.out.println(td1.totalHits);  // prints "1"

        Query q2 = NumericRangeQuery.newIntRange("ticket_id", 1, 234, 234, true, true);
        TopDocs td2 = searcher.search(q2, 1);
        System.out.println(td2.totalHits);  // prints "1"

        // Note to self: That if the numeric precision step is 1 then there is only one result!!!!
        q2 = NumericRangeQuery.newIntRange("ticket_id", 10, 0, 234, true, true);
        td2 = searcher.search(q2, 1);
        System.out.println(td2.totalHits);  // prints "3"
    }

    @Test
    public void exception() {
        try {
            reThrowException();
        } catch (final Exception e) {
            logger.error("", e);
        }
    }

    private void reThrowException() throws Exception {
        try {
            throwException();
        } catch (final Exception e) {
            throw new RuntimeException("Error...", e);
        }
    }

    private void throwException() throws Exception {
        throw new NullPointerException("Bla, bla, bla...");
    }

}