package ikube.action;

import static org.junit.Assert.assertEquals;
import ikube.IConstants;
import ikube.Integration;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.search.SearchSingle;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;

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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
public class IndexDeltaIntegration extends Integration {

	/** Class under test. */
	private IndexDelta indexDelta;
	private IndexContext<?> indexContext;
	private IndexableFileSystem indexableFileSystem;

	@Before
	public void before() {
		indexDelta = new IndexDelta();
		indexableFileSystem = ApplicationContextManager.getBean("desktopFolder");
		indexContext = (IndexContext<?>) indexableFileSystem.getParent();

		IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		clusterManager.getServer().getActions().clear();

		setFields(new Object[] { indexDelta, indexDelta }, new Object[] { dataBase, clusterManager });

		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	private void setFields(final Object[] targets, final Object[] fields) {
		for (int i = 0; i < targets.length; i++) {
			Deencapsulation.setField(targets[i], fields[i]);
		}
	}

	@After
	public void after() {
		// FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void execute() throws Exception {
		File secondDeltaFile = null;
		try {
			String inserted = "Delta file data";
			// Create the index, delta or otherwise
			File deltaFile = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "delta.txt");
			OutputStream outputStream = new FileOutputStream(deltaFile);
			IOUtils.write(inserted.getBytes(), outputStream);
			IOUtils.closeQuietly(outputStream);
			indexableFileSystem.setPath(deltaFile.getParentFile().getAbsolutePath());

			// Indexes the file and results in one hit
			executeDelta(deltaFile);
			verifyIndex(inserted, 2);
			verifyNumDocs(1);

			// Touch the file and it should be re-indexed
			FileUtils.touch(deltaFile);
			executeDelta(deltaFile);
			verifyIndex(inserted, 2);
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
			ArrayList<HashMap<String, String>> results = verifyIndex(random, 3);
			verifyNumDocs(2);

			// No documents should be added after here
			logger.warn("******************** NO DOCUMENTS *************************************");
			executeDelta(secondDeltaFile);
			ArrayList<HashMap<String, String>> secondResults = verifyIndex(random, 3);

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

	private ArrayList<HashMap<String, String>> verifyIndex(final String random, final int resultsSize) throws Exception {
		IndexSearcher indexSearcher = null;
		ArrayList<HashMap<String, String>> results = null;
		try {
			indexSearcher = getIndexSearcher();

			SearchSingle searchSingle = new SearchSingle(indexSearcher);
			searchSingle.setFirstResult(0);
			searchSingle.setFragment(Boolean.TRUE);
			searchSingle.setMaxResults(10);
			searchSingle.setSearchField(IConstants.CONTENTS);
			searchSingle.setSearchString("delta");

			results = searchSingle.execute();
			logger.info("Results : " + results);
			assertEquals("There must be this number of hits for the search string : ", resultsSize, results.size());
			return results;
		} catch (Exception e) {
			logger.error(null, e);
			throw e;
		} finally {
			indexSearcher.getIndexReader().close();
			indexSearcher.close();
		}
	}

	private void verifyNumDocs(final int numDocs) throws Exception {
		IndexReader indexReader = getIndexReader();
		assertEquals("There should be this number of documents in the index : ", numDocs, indexReader.numDocs());
	}

	private IndexSearcher getIndexSearcher() throws Exception {
		IndexReader indexReader = getIndexReader();
		return new IndexSearcher(indexReader);
	}

	private IndexReader getIndexReader() throws Exception {
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File indexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		File indexDirectoryServer = new File(indexDirectory, UriUtilities.getIp());

		Directory directory = FSDirectory.open(indexDirectoryServer);
		IndexReader indexReader = IndexReader.open(directory);
		printIndex(indexReader);
		return indexReader;
	}

	private void executeDelta(final File deltaFile) throws Exception {
		// Execute the delta index
		indexDelta.execute(indexContext);
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