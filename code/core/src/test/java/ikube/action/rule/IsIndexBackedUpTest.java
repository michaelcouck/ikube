package ikube.action.rule;

import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.toolkit.FILE;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29.03.2011
 */
public class IsIndexBackedUpTest extends AbstractTest {

    private IsIndexBackedUp isIndexBackedUp;

    @Before
    public void before() {
        isIndexBackedUp = new IsIndexBackedUp();
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()));
    }

    @After
    public void after() {
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPathBackup()));
    }

    @Test
    public void evaluate() throws IOException {
        File latestIndexDirectory = createIndexFileSystem(indexContext, "some strings");
        boolean result = isIndexBackedUp.evaluate(indexContext);
        assertFalse(result);

        String indexDirectoryBackupPath = IndexManager.getIndexDirectoryPathBackup(indexContext) + "/"
                + latestIndexDirectory.getParentFile().getName();
        File indexDirectoryBackup = new File(indexDirectoryBackupPath);
        FileUtils.copyDirectoryToDirectory(latestIndexDirectory, indexDirectoryBackup);

        result = isIndexBackedUp.evaluate(indexContext);
        assertTrue(result);
    }

}