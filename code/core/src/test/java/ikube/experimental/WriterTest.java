package ikube.experimental;

import com.jcraft.jsch.JSchException;
import ikube.toolkit.THREAD;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Spy;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
public class WriterTest extends AbstractTest {

    @Spy
    private Writer writer;

    @Test
    public void writeToIndex() throws IOException {
        int iterations = 100;
        for (int i = iterations; i > 0; i--) {
            Document document = new Document();
            writer.writeToIndex(document);
        }
        THREAD.sleep(5000);
        Document document = new Document();
        writer.writeToIndex(document);
        Directory[] directories = writer.getDirectories();
        for (final Directory directory : directories) {
            IndexReader indexReader = DirectoryReader.open(directory);
            Assert.assertEquals("There should be a few documents in the index : ", 1 + iterations, indexReader.numDocs());
        }
    }

    @Test
    public void createDocuments() throws SQLException, JSchException {
        List<List<Object>> records = Arrays.asList(
                Arrays.asList((Object) "one", "two", "three"),
                Arrays.asList((Object) "two", "three", "four"),
                Arrays.asList((Object) "three", "four", "five"));
        List<Document> documents = writer.createDocuments(records);
        Assert.assertEquals("one", documents.get(0).getField("0").stringValue());
        Assert.assertEquals("five", documents.get(2).getField("2").stringValue());
    }

}