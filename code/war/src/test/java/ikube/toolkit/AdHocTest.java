package ikube.toolkit;

import ikube.AbstractTest;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore
public class AdHocTest extends AbstractTest {

    @Test
    public void printIndex() throws Exception {
        File indexDirectory = new File("/tmp/indexes/ikube-rules/1405275318836/192.168.1.8/");
        Directory directory = FSDirectory.open(indexDirectory);
        IndexReader indexReader = DirectoryReader.open(directory);
        printIndex(indexReader, 100000);
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