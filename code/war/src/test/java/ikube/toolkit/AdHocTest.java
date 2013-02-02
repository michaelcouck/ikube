package ikube.toolkit;

import ikube.Base;
import ikube.index.spatial.Coordinate;
import ikube.search.SearchSpatial;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class AdHocTest extends Base {

	@Test
	@Ignore
	public void print() throws Exception {
		String indexPath = "/tmp/1359663404552/192.168.122.1-8000";
		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexPath)));
		printIndex(indexReader, 10);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		MultiSearcher multiSearcher = new MultiSearcher(indexSearcher);

		Coordinate coordinate = new Coordinate(52.52274, 13.4166);

		SearchSpatial searchSpatialAll = new SearchSpatial(multiSearcher);
		searchSpatialAll.setCoordinate(coordinate);
		searchSpatialAll.setDistance(10);
		searchSpatialAll.setFirstResult(0);
		searchSpatialAll.setFragment(true);
		searchSpatialAll.setMaxResults(10);
		searchSpatialAll.setSearchField("name");
		searchSpatialAll.setSearchString("hotel");
		searchSpatialAll.setSortField();
		ArrayList<HashMap<String, String>> results = searchSpatialAll.execute();
		logger.info("Results : " + results);

		indexReader.close();
		indexSearcher.close();
	}

}
