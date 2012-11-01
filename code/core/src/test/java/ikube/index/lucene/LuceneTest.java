package ikube.index.lucene;

import static org.junit.Assert.assertEquals;
import ikube.ATest;
import ikube.IConstants;
import ikube.search.SearchSingle;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

/**
 * Various tests for Lucene indexes, including language indexing and searching. This is just a sanity test for language support etc. Can
 * Lucene search for other character sets and are the results in the correct format, things like that, just to stay ahead of the insane.
 * 
 * @author Michael Couck
 * @since 06.03.10
 * @version 01.00
 */
public class LuceneTest extends ATest {

	private String russian = " русский язык  ";
	private String german = "Produktivität";
	private String french = "productivité";
	private String somthingElseAlToGether = "Soleymān Khāţer";
	private String string = "Qu'est ce qui détermine la productivité, et comment est-il mesuré? " //
			+ "Was bestimmt die Produktivität, und wie wird sie gemessen? " //
			+ "русский язык " + //
			"Soleymān Khāţer Solţānābād " + //
			russian + " " + //
			german + " " + //
			french + " " + //
			somthingElseAlToGether + " ";

	public LuceneTest() {
		super(LuceneTest.class);
	}

	@Test
	public void search() throws Exception {
		SearchSingle searchSingle = createIndexAndSearch(SearchSingle.class, IConstants.ANALYZER, IConstants.CONTENTS, russian, german,
				french, somthingElseAlToGether, string);
		searchSingle.setFirstResult(0);
		searchSingle.setFragment(true);
		searchSingle.setMaxResults(10);
		searchSingle.setSearchField(IConstants.CONTENTS);

		ArrayList<HashMap<String, String>> results = null;

		searchSingle.setSearchString(french);
		results = searchSingle.execute();
		assertEquals(3, results.size());

		searchSingle.setSearchString(german + "~");
		results = searchSingle.execute();
		assertEquals(4, results.size());

		searchSingle.setSearchString(russian + "~");
		results = searchSingle.execute();
		assertEquals(3, results.size());

		searchSingle.setSearchString(somthingElseAlToGether);
		results = searchSingle.execute();
		assertEquals(3, results.size());
	}

}