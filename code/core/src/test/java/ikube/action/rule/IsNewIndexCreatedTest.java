package ikube.action.rule;

import ikube.AbstractTest;
import ikube.toolkit.FILE;
import ikube.toolkit.OS;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * This test is for the {@link IsNewIndexCreated} class that will check if there
 * is a newer index than the one that is opened in the index searcher.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-06-2011
 */
public class IsNewIndexCreatedTest extends AbstractTest {

    @After
    public void after() {
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void evaluate() throws Exception {
        // TODO: This doesnt' work on CentOs!!!
        if (!OS.isOs("3.11.0-12-generic")) {
            return;
        }
        when(indexContext.getMultiSearcher()).thenReturn(null);

        // Searcher null
        IsNewIndexCreated isNewIndexCreated = new IsNewIndexCreated();
        boolean result = isNewIndexCreated.evaluate(indexContext);
        assertFalse("No index created : ", result);

        // Index not opened but index created, so new one available
        File latestIndexDirectory = createIndexFileSystem(indexContext, "Some data");
        result = isNewIndexCreated.evaluate(indexContext);
        assertTrue("New index available : ", result);

        Directory directory = NIOFSDirectory.open(latestIndexDirectory);
        IndexReader indexReader = DirectoryReader.open(directory);
        MultiReader multiReader = new MultiReader(indexReader);
        IndexSearcher searcher = new IndexSearcher(multiReader);

        Mockito.when(indexContext.getMultiSearcher()).thenReturn(searcher);
        result = isNewIndexCreated.evaluate(indexContext);
        assertFalse("The latest index is already opened : ", result);

        createIndexFileSystem(indexContext, "Some more data");
        result = isNewIndexCreated.evaluate(indexContext);
        assertTrue("There is a new index, and the searcher is open on the old one : ", result);

        for (int i = 0; i < 10; i++) {
            result = isNewIndexCreated.evaluate(indexContext);
            assertTrue("There is a new index, and the searcher is open on the old one : ", result);
        }
    }

}