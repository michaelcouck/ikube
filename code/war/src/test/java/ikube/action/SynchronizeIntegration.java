package ikube.action;

import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-04-2014
 */
@Ignore
public class SynchronizeIntegration extends IntegrationTest {

	private IndexContext indexContext;

	@Before
	public void before() {
		indexContext = ApplicationContextManager.getBean("desktop");
		IndexableFileSystem indexableFileSystem = ApplicationContextManager.getBean("desktopFolder");
		indexableFileSystem.setPath("/tmp");
	}

	@After
	public void after() throws Exception {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
	}

	@Test
	public void execute() throws Exception {
		// Create the index
		Index index = ApplicationContextManager.getBean(Index.class);
		index.execute(indexContext);
		ThreadUtilities.sleep(15000);
		// Synchronize it to the local files
		Synchronize synchronize = ApplicationContextManager.getBean(Synchronize.class);
		synchronize.execute(indexContext);
		// Verify that it is in fact changed and valid
		Open open = ApplicationContextManager.getBean(Open.class);
		open.execute(indexContext);

		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		Server server = clusterManager.getServer();

		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		File serverIndexDirectory = new File(latestIndexDirectory, server.getAddress());
		logger.info("Latest index directory : " + serverIndexDirectory.getAbsolutePath());
		Directory directory = FSDirectory.open(serverIndexDirectory);
		assertTrue(DirectoryReader.indexExists(directory));
		assertNotNull(indexContext.getMultiSearcher());
	}

}