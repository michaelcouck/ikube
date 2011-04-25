package ikube.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.action.Index;
import ikube.index.spatial.Coordinate;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

public class SearchSpatialTest extends BaseTest {

	public SearchSpatialTest() {
		super(SearchSpatialTest.class);
	}

	@Before
	public void before() throws Exception {
		// Create and index with the spacial data
		Index index = new Index();
		boolean result = index.execute(indexContext);
		assertTrue(result);
	}

	@Test
	public void searchSpatial() throws Exception {
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		File indexDirectory = latestIndexDirectory.listFiles()[0];
		Directory directory = FSDirectory.open(indexDirectory);
		IndexSearcher indexSearcher = new IndexSearcher(directory);
		SearchSpatial searchSpatial = new SearchSpatial(indexSearcher);
		searchSpatial.setFirstResult(0);
		searchSpatial.setFragment(Boolean.TRUE);
		searchSpatial.setMaxResults(100);
		searchSpatial.setSearchField("content");
		searchSpatial.setSearchString("content");
		searchSpatial.setSortField("content");
		searchSpatial.setCoordinate(new Coordinate(51.0589216, 3.7243959));
		List<Map<String, String>> results = searchSpatial.execute();
		assertNotNull(results);
	}

}
