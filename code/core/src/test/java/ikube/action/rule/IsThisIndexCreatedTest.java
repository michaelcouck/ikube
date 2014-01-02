package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.action.index.IndexManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 14.07.11
 * @version 01.00
 */
public class IsThisIndexCreatedTest extends AbstractTest {

	private IsThisIndexCreated isThisIndexCreated;

	@Before
	public void before() {
		isThisIndexCreated = new IsThisIndexCreated();
		Deencapsulation.setField(isThisIndexCreated, clusterManager);
		when(indexContext.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void execute() throws Exception {
		boolean isIndexCreated = isThisIndexCreated.evaluate(indexContext);
		assertFalse("This index should not be created yet : ", isIndexCreated);

		// Create an index and lock it
		Lock lock = null;
		createIndexFileSystem(indexContext, "Some data : ");
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		File indexDirectory = new File(latestIndexDirectory, UriUtilities.getIp());
		try {
			lock = getLock(FSDirectory.open(indexDirectory), indexDirectory);
			isIndexCreated = isThisIndexCreated.evaluate(indexContext);
			assertTrue("This index should be created : ", isIndexCreated);
		} finally {
			lock.release();
		}

		// Index now unlocked but still exists
		isIndexCreated = isThisIndexCreated.evaluate(indexContext);
		assertTrue("This index should be created : ", isIndexCreated);
	}

}
