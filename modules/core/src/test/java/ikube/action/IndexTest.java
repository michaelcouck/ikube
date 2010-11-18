package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.search.SearchMulti;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

public class IndexTest extends BaseTest {

	private Index index;

	@Before
	public void before() {
		this.index = new Index();
	}

	@Test
	public void execute() throws Exception {
		long maxAge = indexContext.getMaxAge();
		indexContext.setMaxAge(0);

		boolean done = index.execute(indexContext);
		assertTrue(done);

		indexContext.setMaxAge(maxAge);

		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		File serverIndexDirectory = new File(latestIndexDirectory, ip);
		File contextIndexDirectory = new File(serverIndexDirectory, indexContext.getName());
		Directory directory = FSDirectory.open(contextIndexDirectory);
		IndexReader indexReader = IndexReader.open(directory);
		assertTrue(indexReader.numDocs() > 0);

		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		SearchMulti searchMulti = new SearchMulti(indexSearcher);
		searchMulti.setFirstResult(0);
		searchMulti.setFragment(true);
		searchMulti.setMaxResults(10);
		searchMulti.setSearchField(IConstants.CONTENTS, "creator", "modifier");
		searchMulti.setSearchString("acrylic also because definately baklava potpie", "morale urban", "unwrap indignation ");

		List<Map<String, String>> results = searchMulti.execute();
		assertTrue(results.size() > 1);
		for (Map<String, String> result : results) {
			logger.warn("Result : ");
			for (String key : result.keySet()) {
				logger.warn("        : key : " + key);
				logger.warn("        : value : " + result.get(key));
			}
		}

		indexReader.close();
		indexSearcher.close();

		FileUtilities.deleteFile(latestIndexDirectory, 1);
	}

}
