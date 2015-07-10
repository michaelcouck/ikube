package ikube.experimental;

import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.Test;
import org.mockito.Spy;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
public class SearcherTest extends AbstractTest {

    @Spy
    private Searcher searcher;

    @Test
    public void openSearcher() throws IOException {
        int numberOfDirectories = 3;
        Directory[] directories = getDirectories(numberOfDirectories);
        searcher.openSearcher(directories);

        IndexSearcher indexSearcher = searcher.getSearcher();
        assertNotNull(indexSearcher);

        MultiReader multiReader = (MultiReader) indexSearcher.getIndexReader();
        assertNotNull(multiReader);
        assertEquals(numberOfDirectories * numberOfDirectories, multiReader.numDocs());
    }

}
