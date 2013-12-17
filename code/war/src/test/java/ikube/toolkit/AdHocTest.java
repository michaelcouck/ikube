package ikube.toolkit;

import ikube.BaseTest;
import ikube.action.Open;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.search.SearchSpatial;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;

public class AdHocTest extends BaseTest {

	@Test
	@Ignore
	public void print() throws Exception {
		// /tmp/twitter/1387272267605/192.168.1.8-8020/
		// /mnt/sdb/indexes/geospatial/1387303685578/192.168.1.8-8020/
		// File file = new File("/tmp/twitter/1387272267605/192.168.1.8-8020/");
		// File file = new File("/mnt/sdb/indexes/geospatial/1387303685578/192.168.1.8-8020/");
		File file = new File("/mnt/sdb/indexes/twitter/1387311291735/192.168.1.8/");
		Directory directory = FSDirectory.open(file);
		IndexReader indexReader = IndexReader.open(directory);
		printIndex(indexReader, 1000);
		indexReader.close();

		Open open = new Open();
		IndexContext<?> indexContext = new IndexContext<>();
		indexContext.setIndexName("twitter");
		indexContext.setIndexDirectoryPath("/mnt/sdb/indexes/");

		open.execute(indexContext);

		SearchSpatial searchSpatial = new SearchSpatial(indexContext.getMultiSearcher());
		searchSpatial.setDistance(20);
		searchSpatial.setFirstResult(0);
		searchSpatial.setMaxResults(10);
		searchSpatial.setFragment(Boolean.TRUE);
		searchSpatial.setSearchField("contents");
		searchSpatial.setSearchString("twitter");
		Coordinate coordinate = new Coordinate(41.25444, -95.97724);
		searchSpatial.setCoordinate(coordinate);
		
		ArrayList<HashMap<String, String>> results = searchSpatial.execute();
		printResults(results);
	}
	
	private void printResults(final ArrayList<HashMap<String, String>> results) {
		for (final HashMap<String, String> result : results) {
			logger.info("Result : ");
			for (final Map.Entry<String, String> mapEntry : result.entrySet()) {
				logger.info("       : " + mapEntry.getKey() + "-" + mapEntry.getValue());
			}
		}
	}

}
