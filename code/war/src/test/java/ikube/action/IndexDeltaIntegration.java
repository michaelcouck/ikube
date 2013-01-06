package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.Integration;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.service.ISearcherService;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import mockit.Deencapsulation;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 05.01.12
 * @version 01.00
 */
public class IndexDeltaIntegration extends Integration {

	private Open open;
	/** Class under test. */
	private IndexDelta indexDelta;
	private IndexContext<?> indexContext;
	private IndexableFileSystem indexableFileSystem;
	private ISearcherService searcherService;

	@Before
	public void before() {
		open = new Open();
		indexDelta = new IndexDelta();
		indexableFileSystem = ApplicationContextManager.getBean("desktopFolder");
		indexContext = (IndexContext<?>) indexableFileSystem.getParent();
		logger.info("File system : " + indexableFileSystem + ", " + indexableFileSystem.getStrategies() + ", " + indexContext + ", "
				+ indexContext.getIndexName());
		searcherService = ApplicationContextManager.getBean(ISearcherService.class);

		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.getServer().getActions().clear();

		Deencapsulation.setField(open, dataBase);
		Deencapsulation.setField(open, clusterManager);
		Deencapsulation.setField(indexDelta, dataBase);
		Deencapsulation.setField(indexDelta, clusterManager);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void execute() throws Exception {
		// Create the index, delta or otherwise
		File deltaFile = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "delta.txt");
		indexableFileSystem.setPath(deltaFile.getParentFile().getAbsolutePath());

		logger.error("Executing first : ");
		indexDelta.execute(indexContext);
		ThreadUtilities.sleep(10000);
		open.execute(indexContext);
		printIndex(indexContext.getMultiSearcher());

		// Modify a file on the file system
		String random = appendRandomString(deltaFile);
		// Execute the delta index
		logger.error("Executing delta : ");
		indexDelta.execute(indexContext);
		ThreadUtilities.sleep(10000);
		open.execute(indexContext);
		// Verify that only the changed file was updated
		printIndex(indexContext.getMultiSearcher());

		// Verify that the new data is searchable
		ArrayList<HashMap<String, String>> results = searcherService
				.searchMultiAll("desktop", new String[] { random }, Boolean.TRUE, 0, 10);
		logger.info("Results : " + results);
		assertTrue(results.size() == 2);
	}

	private String appendRandomString(final File deltaFile) throws Exception {
		String random = UUID.randomUUID().toString();
		FileOutputStream fileOutputStream = new FileOutputStream(deltaFile, Boolean.TRUE);
		fileOutputStream.write("\n".getBytes());
		fileOutputStream.write(random.getBytes());
		IOUtils.closeQuietly(fileOutputStream);
		return random;
	}

}