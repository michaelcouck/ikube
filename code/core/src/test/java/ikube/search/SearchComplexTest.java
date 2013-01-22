package ikube.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.ATest;
import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.StringUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 20.02.2012
 * @version 01.00
 */
public class SearchComplexTest extends ATest {

	private SearchComplex searchComplex;
        private IndexSearcher indexSearcher;
        private IndexWriter indexWriter;

	public SearchComplexTest() {
		super(SearchComplexTest.class);
	}

	@Before
	public void before() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "index-data.csv");
		File indexDirectory = FileUtilities.getFile("./indexes", Boolean.TRUE);
		indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.TRUE);
		LineIterator lineIterator = FileUtils.lineIterator(file, IConstants.ENCODING);
		String headerLine = lineIterator.nextLine();
		String[] columns = StringUtils.split(headerLine, ',');
		while (lineIterator.hasNext()) {
			String line = lineIterator.nextLine();
			String[] values = StringUtils.split(line, ',');
			Document document = new Document();
			for (int i = 0; i < columns.length && i < values.length; i++) {
				if (StringUtilities.isNumeric(values[i])) {
					IndexManager.addNumericField(columns[i], values[i], document, Store.YES);
				} else {
					IndexManager.addStringField(columns[i], values[i], document, Store.YES, Index.ANALYZED, TermVector.NO);
				}
			}
			indexWriter.addDocument(document);
		}
		IndexReader indexReader = IndexReader.open(indexWriter, Boolean.TRUE);
		indexSearcher = new IndexSearcher(indexReader);
		searchComplex = new SearchComplex(indexSearcher);
	}

	@After
	public void after() throws Exception {
                indexWriter.close();
		FileUtilities.deleteFile(new File("./indexes"), 1);
                indexSearcher.close();
	}

	@Test
	public void getQueryAndSearch() throws ParseException {
		// TODO When we get the min-eco data with the coordinates
		// then update this test to search against the geospatial data too
		// Coordinate coordinate = new Coordinate(52.52274, 13.4166);
		searchComplex.setCoordinate(null);
		searchComplex.setDistance(10);
		searchComplex.setFirstResult(0);
		searchComplex.setFragment(Boolean.TRUE);
		searchComplex.setMaxResults(10);
		searchComplex.setSearchField("NAME", "ANNEE", "NR_KBO_BCE_CO_NR");
		searchComplex.setSearchString("INTERCOMMUNALE", "2000", "200362210-200952524");
		searchComplex.setTypeFields("string", "numeric", "range");
		Query query = searchComplex.getQuery();
		assertNotNull(query);

		ArrayList<HashMap<String, String>> results = searchComplex.execute();
		assertEquals("There must be 4 results and the statistics : ", 5, results.size());
	}

}
