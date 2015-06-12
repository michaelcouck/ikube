package ikube.toolkit;

import ikube.AbstractTest;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.ArrayList;

import static org.mockito.Mockito.when;

/**
 * This test just has to run without exception, the number of index files that are
 * merged seems to be random, and Lucene decides, strangely enough.
 * 
 * @author Michael Couck
 * @since 08-02-2013
 * @version 01.00
 */
public class OptimizerTest extends AbstractTest {

    @Mock
    private IndexContext indexContext;
	/** "192.168.1.2", "192.168.1.3" */
	private String[] ips = { "192.168.1.1" };

	@Before
	public void before() {
        when(indexContext.getIndexDirectoryPath()).thenReturn("./indexes");
        when(indexContext.getIndexDirectoryPathBackup()).thenReturn("./indexes/backup");
        when(indexContext.getName()).thenReturn("index");
        when(indexContext.getIndexName()).thenReturn("index");
        when(indexContext.getChildren()).thenReturn(new ArrayList<Indexable>());

        when(indexContext.getBufferedDocs()).thenReturn(10);
        when(indexContext.getBufferSize()).thenReturn(10d);
        when(indexContext.getMaxFieldLength()).thenReturn(10);
        when(indexContext.getMaxReadLength()).thenReturn(10000l);
        when(indexContext.getMergeFactor()).thenReturn(10);
        when(indexContext.getMaxAge()).thenReturn(60l);

        for (final String ip : ips) {
            createIndexFileSystem(indexContext, System.currentTimeMillis(), ip, "hello world");
        }
    }

	@After
	public void after() {
		FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()));
	}

	@Test
	public void main() {
		for (final String ip : ips) {
			File indexDirectory = FILE.findDirectoryRecursively(new File("."), new String[] {ip});
			logger.info("Index directory : " + indexDirectory.getAbsolutePath());
			String[] args = { FILE.cleanFilePath(indexDirectory.getAbsolutePath()) };
			Optimizer.main(args);
		}
	}

}
