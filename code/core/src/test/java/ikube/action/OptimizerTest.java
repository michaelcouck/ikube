package ikube.action;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

/**
 * This test just has to run without exception, the number of index files
 * that are merged seems to be random, and Lucene decides, strangely enough, so there
 * is no real way to see that the index was actually optimized.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2013
 */
public class OptimizerTest extends AbstractTest {

    /**
     * "192.168.1.2", "192.168.1.3"
     */
    private String[] ips = {"192.168.1.1"};

    @Before
    public void before() {
        createIndexesFileSystem(indexContext, System.currentTimeMillis(), ips, "and a little data");
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void optimize() throws Exception {
        new Optimizer().execute(indexContext);
        Mockito.verify(indexContext, Mockito.atLeastOnce()).getAnalyzer();
    }

}
