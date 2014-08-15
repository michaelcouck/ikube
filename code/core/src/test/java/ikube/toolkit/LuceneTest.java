package ikube.toolkit;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.search.Search;
import ikube.search.SearchComplex;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Various tests for Lucene indexes, including language indexing and searching. This is just a sanity test for
 * language support etc. Can Lucene search for other character sets and are the results in the correct format,
 * things like that, just to stay ahead of the insane.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 06-03-2010
 */
@SuppressWarnings("deprecation")
public class LuceneTest extends AbstractTest {

    class Writer implements Runnable {

        final IndexWriter indexWriter;
        final int documents;

        Writer(final IndexWriter indexWriter, final int documents) {
            this.indexWriter = indexWriter;
            this.documents = documents;
        }

        public void run() {
            try {
                int index = documents;
                while (index-- > 0) {
                    String identifier = Long.toHexString(System.currentTimeMillis());
                    Document document = getDocument(identifier, LuceneTest.this.string, IConstants.CONTENTS);
                    this.indexWriter.addDocument(document);
                    this.indexWriter.commit();
                    this.indexWriter.forceMerge(5, Boolean.FALSE);
                    logger.debug("Adding document : " + this.indexWriter.numDocs());
                    ThreadUtilities.sleep(10);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String russian = " русский язык  ";
    private String german = "Produktivität";
    private String french = "productivité";
    private String somethingElseAlToGether = "Soleymān Khāţer";
    private String string = "Qu'est ce qui détermine la productivité, et comment est-il mesuré? " //
            + "Was bestimmt die Produktivität, und wie wird sie gemessen? " //
            + "русский язык " + //
            "Soleymān Khāţer Solţānābād " + //
            russian + " " + //
            german + " " + //
            french + " " + //
            somethingElseAlToGether + " ";

    @Before
    public void before() {
        ThreadUtilities.initialize();
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
    }

    @Test
    public void search() throws Exception {
        String somethingNumeric = " 123456789 ";
        String somethingElseNumeric = " 1234567890 ";
        SearchComplex searchSingle = createIndexRamAndSearch(SearchComplex.class, new StemmingAnalyzer(),
                IConstants.CONTENTS, russian, german, french, somethingElseAlToGether, string, somethingNumeric,
                somethingElseNumeric);
        searchSingle.setFirstResult(0);
        searchSingle.setFragment(true);
        searchSingle.setMaxResults(10);
        searchSingle.setSearchFields(IConstants.CONTENTS);

        ArrayList<HashMap<String, String>> results;

        searchSingle.setSearchStrings(french);
        results = searchSingle.execute();
        assertEquals(3, results.size());

        searchSingle.setSearchStrings(german + "~");
        results = searchSingle.execute();
        assertEquals(3, results.size());

        searchSingle.setSearchStrings(russian + "~");
        results = searchSingle.execute();
        assertEquals(3, results.size());

        searchSingle.setSearchStrings(somethingElseAlToGether);
        results = searchSingle.execute();
        assertEquals(3, results.size());
    }

    @Test
    @SuppressWarnings("UnnecessaryBoxing")
    public void numericSearch() throws Exception {
        final int integer = 123456789;
        Directory directory = createIndex(IConstants.ID, IConstants.CONTENTS, integer, integer);
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = NumericRangeQuery.newIntRange(IConstants.ID, integer, integer, Boolean.TRUE, Boolean.TRUE);
        TopDocs topDocs = indexSearcher.search(query, 10);
        assertEquals("There must be exactly one result from the search : ", 1, topDocs.scoreDocs.length);

        query = NumericRangeQuery.newDoubleRange(IConstants.CONTENTS, Double.valueOf(integer), Double.valueOf(integer), Boolean.TRUE, Boolean.TRUE);
        topDocs = indexSearcher.search(query, 10);
        assertEquals("There must be exactly one result from the search : ", 1, topDocs.scoreDocs.length);
    }

    @Test
    public void rangeSearch() throws Exception {
        final int integer = 123456789;
        Directory directory = createIndex(IConstants.ID, IConstants.CONTENTS, integer, integer);
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        Query query = NumericRangeQuery.newIntRange(IConstants.ID, Integer.MIN_VALUE, Integer.MAX_VALUE, Boolean.TRUE, Boolean.TRUE);
        TopDocs topDocs = indexSearcher.search(query, 10);
        assertEquals("There must be exactly one result from the search : ", 1, topDocs.scoreDocs.length);
    }

    private Directory createIndex(final String fieldName, final String doubleFieldName, final int fieldValue, final double doubleFieldValue) throws IOException {
        Analyzer analyzer = new StandardAnalyzer(IConstants.LUCENE_VERSION);
        IndexWriterConfig conf = new IndexWriterConfig(IConstants.LUCENE_VERSION, analyzer);
        Directory directory = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(directory, conf);

        Document document = new Document();

        FieldType fieldType = new FieldType();
        fieldType.setStored(Boolean.TRUE);
        fieldType.setIndexed(Boolean.TRUE);
        fieldType.setTokenized(Boolean.TRUE);
        fieldType.setNumericType(FieldType.NumericType.INT);

        Field field = new IntField(fieldName, fieldValue, fieldType);
        document.add(field);

        FieldType doubleFieldType = new FieldType();
        doubleFieldType.setStored(Boolean.TRUE);
        doubleFieldType.setIndexed(Boolean.TRUE);
        doubleFieldType.setTokenized(Boolean.TRUE);
        doubleFieldType.setNumericType(FieldType.NumericType.DOUBLE);
        Field doubleField = new DoubleField(doubleFieldName, doubleFieldValue, doubleFieldType);
        document.add(doubleField);

        indexWriter.addDocument(document, analyzer);
        indexWriter.commit();
        indexWriter.forceMerge(5);

        return directory;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void concurrentReadAndWriteToIndex() throws Exception {
        final long sleep = 100;
        final int iterations = 3;
        final File indexDirectory = createIndexFileSystem(indexContext, IConstants.CONTENTS);
        final IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.FALSE);

        List<Future<Object>> futures = new ArrayList<>();
        class Searcher implements Runnable {
            @Override
            public void run() {
                try {
                    int previousSize = 0;
                    int index = iterations * 100;
                    while (index-- > 0) {
                        ThreadUtilities.sleep(sleep);

                        IndexReader reader = DirectoryReader.open(indexWriter, Boolean.TRUE);
                        IndexSearcher indexSearcher = new IndexSearcher(reader);

                        SearchComplex searchSingle = new SearchComplex(indexSearcher);
                        searchSingle.setFirstResult(0);
                        searchSingle.setFragment(Boolean.TRUE);
                        searchSingle.setMaxResults(Integer.MAX_VALUE);
                        searchSingle.setSearchFields(IConstants.CONTENTS);
                        searchSingle.setSearchStrings("détermine");
                        searchSingle.setOccurrenceFields(IConstants.SHOULD);
                        searchSingle.setTypeFields(Search.TypeField.STRING.name());
                        searchSingle.setSortFields(IConstants.CONTENTS);
                        ArrayList<HashMap<String, String>> results = searchSingle.execute();
                        int size = results.size();
                        logger.debug("Results size : " + size);
                        assertTrue(size >= previousSize);
                        previousSize = size;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Writer writer = new Writer(indexWriter, Integer.MAX_VALUE);
        ThreadUtilities.submit(null, writer);
        for (int i = 0; i < 3; i++) {
            Searcher searcher = new Searcher();
            Future<Object> future = (Future<Object>) ThreadUtilities.submit(Integer.toHexString(i), searcher);
            futures.add(future);
        }
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void searcherManagerReadAndWrite() throws Exception {
        final long sleep = 100;
        final int iterations = 3;
        long time = System.currentTimeMillis();
        File indexDirectory = createIndexFileSystem(indexContext, time, "127.0.0.1", "the", "quick", "brown", "fox", "jumped");
        final IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, false);
        final SearcherManager searcherManager = new SearcherManager(indexWriter, true, new SearcherFactory());

        class Searcher implements Runnable {
            @Override
            public void run() {
                try {
                    int previousSize = 0;
                    int index = iterations * 100;
                    while (index-- > 0) {
                        ThreadUtilities.sleep(sleep);
                        IndexSearcher indexSearcher = searcherManager.acquire();
                        SearchComplex searchSingle = new SearchComplex(indexSearcher);
                        searchSingle.setFirstResult(0);
                        searchSingle.setFragment(Boolean.TRUE);
                        searchSingle.setMaxResults(Integer.MAX_VALUE);
                        searchSingle.setSearchFields(IConstants.CONTENTS);
                        searchSingle.setSearchStrings("détermine");
                        searchSingle.setOccurrenceFields(IConstants.SHOULD);
                        searchSingle.setTypeFields(Search.TypeField.STRING.name());
                        searchSingle.setSortFields(IConstants.CONTENTS);
                        ArrayList<HashMap<String, String>> results = searchSingle.execute();
                        int size = results.size();
                        searcherManager.release(indexSearcher);
                        searcherManager.maybeRefresh();
                        assertTrue(size >= previousSize);
                        previousSize = size;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        List<Future<Object>> futures = new ArrayList<>();
        Runnable writer = new Writer(indexWriter, Integer.MAX_VALUE);
        ThreadUtilities.submit(null, writer);
        for (int i = 0; i < 3; i++) {
            Runnable searcher = new Searcher();
            Future<Object> future = (Future<Object>) ThreadUtilities.submit(Integer.toHexString(i), searcher);
            futures.add(future);
        }
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
    }


}