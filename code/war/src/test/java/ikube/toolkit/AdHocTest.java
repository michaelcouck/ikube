package ikube.toolkit;

import com.googlecode.flaxcrawler.CrawlerController;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
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
    public void search() throws Exception {
        File latestIndexDirectory = new File("/tmp/indexes/artvens/1413103866703/192.168.1.8-8000");
        Directory directory = FSDirectory.open(latestIndexDirectory);
        try (IndexReader indexReader = DirectoryReader.open(directory)) {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            // new StemmingAnalyzer()
            SearchComplex searchComplex = new SearchComplex(indexSearcher);
            searchComplex.setFirstResult(0);
            searchComplex.setMaxResults(10);
            searchComplex.setFragment(Boolean.TRUE);

            searchComplex.setSearchStrings("artvens");
            searchComplex.setTypeFields(STRING);
            searchComplex.setOccurrenceFields(MUST);
            searchComplex.setSearchFields(CONTENT);

            ArrayList<HashMap<String, String>> results = searchComplex.execute();

            for (final HashMap<String, String> hashMap : results) {
                logger.error("Results : " + hashMap);
            }
        }
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
                new IndexWriterConfig(IConstants.LUCENE_VERSION, new ClassicAnalyzer(IConstants.LUCENE_VERSION))
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

    @Test
    public void staticFinalField() {
        CrawlerController crawlerController = new CrawlerController(null);
        OBJECT.setField(crawlerController, "STATS_DB_DIR", "whatever");
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