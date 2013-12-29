package ikube.action.rule;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * This test is for the {@link IsNewIndexCreated} class that will check if there is a newer index than the one that is
 * opened in the index searcher.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11.06.11
 */
public class IsNewIndexCreatedTest extends AbstractTest {

    private String originalDirectoryPath;

    @Before
    public void before() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
        originalDirectoryPath = indexContext.getIndexDirectoryPath();
        when(indexContext.getIndexDirectoryPath()).thenReturn("./" + getClass().getSimpleName());
        // when(indexReader.directory()).thenReturn(fsDirectory);
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
        when(indexContext.getIndexDirectoryPath()).thenReturn(originalDirectoryPath);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void evaluate() throws Exception {
        // final IndexContext<?> indexContext
        IsNewIndexCreated isNewIndexCreated = new IsNewIndexCreated();
        boolean result = isNewIndexCreated.evaluate(indexContext);
        assertFalse("There is no index created : ", result);

        File indexDirectory = createIndexFileSystem(indexContext, "Some data");

        result = isNewIndexCreated.evaluate(indexContext);
        assertTrue("The index is created : ", result);

        // Open the index searcher on the latest index
        when(fsDirectory.getDirectory()).thenReturn(indexDirectory);
        result = isNewIndexCreated.evaluate(indexContext);
        assertFalse("The latest index is already opened : ", result);

        createIndexFileSystem(indexContext, "Some more data");
        result = isNewIndexCreated.evaluate(indexContext);
        assertTrue("There is a new index, and the searcher is open on the old one : ", result);
    }

}