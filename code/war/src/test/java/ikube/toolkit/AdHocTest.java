package ikube.toolkit;

import ikube.BaseTest;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

@Ignore
public class AdHocTest extends BaseTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws IOException {
    }

    @Test
    public void printIndex() throws Exception {
        File indexDirectory = new File("/mnt/sdb/indexes/geospatial/1392390234010/192.168.1.8-8020");
        Directory directory = FSDirectory.open(indexDirectory);
        IndexReader indexReader = DirectoryReader.open(directory);
        printIndex(indexReader, 100);
        indexReader.close();
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