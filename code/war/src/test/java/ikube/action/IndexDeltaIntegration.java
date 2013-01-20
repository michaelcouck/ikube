package ikube.action;

import static org.junit.Assert.assertEquals;
import ikube.IConstants;
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Deencapsulation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 05.01.12
 * @version 01.00
 */
@Ignore
@SuppressWarnings("deprecation")
public class IndexDeltaIntegration extends Integration {

	private Open open;
	/** Class under test. */
	private IndexDelta indexDelta;
	private IndexContext<?> indexContext;
	private ISearcherService searcherService;
	private IndexableFileSystem indexableFileSystem;

	@Before
	public void before() {
		open = new Open();
		indexDelta = new IndexDelta();
		indexableFileSystem = ApplicationContextManager.getBean("desktopFolder");
		indexContext = (IndexContext<?>) indexableFileSystem.getParent();
		searcherService = ApplicationContextManager.getBean(ISearcherService.class);

		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.getServer().getActions().clear();

		setFields(new Object[] { open, open, indexDelta, indexDelta }, new Object[] { dataBase, clusterManager, dataBase, clusterManager });
	}

	private void setFields(final Object[] targets, final Object[] fields) {
		for (int i = 0; i < targets.length; i++) {
			Deencapsulation.setField(targets[i], fields[i]);
		}
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void execute() throws Exception {
		File secondDeltaFile = null;
		try {
			// Create the index, delta or otherwise
			File deltaFile = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "delta.txt");
			OutputStream outputStream = new FileOutputStream(deltaFile);
			IOUtils.write("Delta file".getBytes(), outputStream);
			IOUtils.closeQuietly(outputStream);
			indexableFileSystem.setPath(deltaFile.getParentFile().getAbsolutePath());

			// Indexes the file and results in one hit
			executeDelta(deltaFile);
			verifyIndex("Delta file", 2);
			verifyNumDocs(1);

			// Touch the file and it should be re-indexed
			FileUtils.touch(deltaFile);
			executeDelta(deltaFile);
			verifyIndex("Delta file", 2);
			verifyNumDocs(1);

			// Modify a file on the file system
			String random = appendRandomString(deltaFile);
			// Re-indexes the file, deletes the old entry and results in still one hit
			executeDelta(deltaFile);
			verifyIndex(random, 2);
			verifyNumDocs(1);

			// Now add a file with the same name in a different folder
			StringBuilder stringBuilder = new StringBuilder(deltaFile.getParentFile().getAbsolutePath());
			stringBuilder.append(IConstants.SEP);
			stringBuilder.append("delta-folder");
			stringBuilder.append(IConstants.SEP);
			stringBuilder.append(deltaFile.getName());

			secondDeltaFile = FileUtilities.getFile(stringBuilder.toString(), Boolean.FALSE);
			outputStream = new FileOutputStream(secondDeltaFile);
			IOUtils.write("Second delta file".getBytes(), outputStream);
			IOUtils.closeQuietly(outputStream);

			random = appendRandomString(secondDeltaFile);
			// Only a hit from the second delta file
			executeDelta(secondDeltaFile);
			logger.info("Searching for random : " + random);
			ArrayList<HashMap<String, String>> results = verifyIndex(random, 2);
			verifyNumDocs(2);

			// No documents should be added after here
			logger.warn("******************** NO DOCUMENTS *************************************");
			executeDelta(secondDeltaFile);
			ArrayList<HashMap<String, String>> secondResults = verifyIndex(random, 2);

			// Verify that the length and the time stamps are the same and the number in the index
			for (int i = 0; i < results.size(); i++) {
				HashMap<String, String> result = results.get(i);
				HashMap<String, String> secondResult = secondResults.get(i);
				for (String key : result.keySet()) {
					if (IConstants.DURATION.equals(key)) {
						continue;
					}
					logger.info("Key : " + key + ", " + result.get(key) + ", " + secondResult.get(key));
					assertEquals(result.get(key), secondResult.get(key));
				}
			}
		} finally {
			if (secondDeltaFile != null) {
				FileUtilities.deleteFile(secondDeltaFile.getParentFile(), 1);
			}
		}
	}

	private ArrayList<HashMap<String, String>> verifyIndex(final String random, final int resultsSize) {
		// Verify that the new data is searchable
		ArrayList<HashMap<String, String>> results = searcherService
				.searchMultiAll("desktop", new String[] { random }, Boolean.TRUE, 0, 10);
		logger.info("Results : " + results);
		assertEquals("There must be this number of hits for the search string : ", resultsSize, results.size());
		return results;
	}

	private void verifyNumDocs(final int numDocs) {
		int totalNumDocs = 0;
		MultiSearcher multiSearcher = indexContext.getMultiSearcher();
		Searchable[] searchables = multiSearcher.getSearchables();
		for (final Searchable searchable : searchables) {
			IndexReader indexReader = ((IndexSearcher) searchable).getIndexReader();
			totalNumDocs += indexReader.numDocs();
		}
		assertEquals("There should be this number of documents in the index : ", numDocs, totalNumDocs);
	}

	private void executeDelta(final File deltaFile) throws Exception {
		// Execute the delta index
		indexDelta.execute(indexContext);
		ThreadUtilities.sleep(5000);
		open.execute(indexContext);
		ThreadUtilities.sleep(5000);
		// Verify that only the changed file was updated
		printIndex(indexContext.getMultiSearcher());
	}

	private String appendRandomString(final File deltaFile) throws Exception {
		String random = RandomStringUtils.randomAlphabetic(10);
		FileOutputStream fileOutputStream = new FileOutputStream(deltaFile, Boolean.TRUE);
		fileOutputStream.write("\n".getBytes());
		fileOutputStream.write(random.getBytes());
		IOUtils.closeQuietly(fileOutputStream);
		return random;
	}

}