package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.ApplicationContextManager;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
public class SearcherTest extends ATest {

	public SearcherTest() {
		super(SearcherTest.class);
	}

	@Test
	public void validate() throws Exception {
		Searcher searcher = ApplicationContextManager.getBean(Searcher.class);
		boolean result = searcher.execute(INDEX_CONTEXT);
		assertTrue(result);
		// TODO Check that there is a mail
	}
}