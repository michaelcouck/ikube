package ikube.action.rule;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static ikube.toolkit.FileUtilities.getOrCreateDirectory;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-03-2011
 */
public class AreUnopenedIndexesTest extends AbstractTest {

    /**
     * Class under test
     */
    private AreUnopenedIndexes areUnopenedIndexes;

    @Before
    public void before() {
        areUnopenedIndexes = new AreUnopenedIndexes();
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void evaluate() throws Exception {
        IndexSearcher searcher = null;
        try {
            File latestIndexDirectory = createIndexFileSystem(indexContext, "some words to index");

            Directory directory = NIOFSDirectory.open(latestIndexDirectory);
            IndexReader indexReader = DirectoryReader.open(directory);
            MultiReader multiReader = new MultiReader(indexReader);
            searcher = new IndexSearcher(multiReader);

            when(indexContext.getMultiSearcher()).thenReturn(searcher);

            Boolean result = areUnopenedIndexes.evaluate(indexContext);
            assertFalse(result);

			File parentDirectory = getOrCreateDirectory(new File(latestIndexDirectory.getParent(), "127.0.0.1.1234567890"));
            FileUtils.copyDirectory(latestIndexDirectory, parentDirectory);
            result = areUnopenedIndexes.evaluate(indexContext);
            assertTrue(result);

            IndexReader indexReaderTwo = DirectoryReader.open(directory);
            multiReader = new MultiReader(indexReader, indexReaderTwo);
            searcher = new IndexSearcher(multiReader);

            when(indexContext.getMultiSearcher()).thenReturn(searcher);
            result = areUnopenedIndexes.evaluate(indexContext);
            assertFalse(result);
        } finally {
            if (searcher != null && searcher.getIndexReader() != null) {
                searcher.getIndexReader().close();
            }
        }
    }

}