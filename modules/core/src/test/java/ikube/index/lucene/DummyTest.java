package ikube.index.lucene;

import ikube.IConstants;
import ikube.logging.Logging;
import ikube.search.SearchSingle;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Ignore;
import org.junit.Test;



public class DummyTest {

	static {
		Logging.configure();
	}

	private Logger logger = Logger.getLogger(this.getClass());

	@Test
	@Ignore
	public void search() throws Exception {
		String filePath = "./index/1288733070037/jackal";
		File indexDirectory = FileUtilities.getFile(filePath, Boolean.TRUE);
		Directory directory = FSDirectory.open(indexDirectory);
		IndexReader reader = IndexReader.open(directory);

		IndexSearcher indexSearcher = new IndexSearcher(reader);
		Searchable[] searchables = new Searchable[] { indexSearcher };
		MultiSearcher searcher = new MultiSearcher(searchables);

		SearchSingle searchSingle = new SearchSingle(searcher);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchField(IConstants.CONTENTS);

		searchSingle.setSearchString("tremor");
		List<Map<String, String>> results = searchSingle.execute();
		logger.warn(results);
	}

}
