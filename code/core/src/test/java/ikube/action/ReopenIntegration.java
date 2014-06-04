package ikube.action;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import org.apache.lucene.index.IndexWriter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-05-2014
 */
public class ReopenIntegration extends AbstractTest {

    @Test
    public void memoryValidation() throws Exception {
        // Check that the memory stays constant when re-opening the indexes
        IndexWriter[] indexWriters = IndexManager.openIndexWriterDelta(indexContext);
        indexContext.setIndexWriters(indexWriters);
        // Now add documents and reopen every ten seconds for a million documents
        System.gc();
        long before = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / IConstants.MILLION;
        for (int i = 1000; i >= 0; i--) {
            if (i > 0 && i % 1000 == 0) {
                System.gc();
                indexWriters[0].commit();
                indexWriters[0].forceMerge(10, Boolean.TRUE);
                new Reopen().execute(indexContext);
                System.gc();
                long after = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / IConstants.MILLION;
                long increase = (after - before);
                logger.info("Before : " + before + ", after : " + after + ", increase : " + increase);
            }
            addDocuments(indexWriters[0], IConstants.CONTENTS, "Michael Couck again");
        }
        System.gc();
        long after = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / IConstants.MILLION;
        long increase = (after - before);
        logger.info("Before : " + before + ", " + after + ", " + increase);
        assertTrue(increase < 100);
    }

}
