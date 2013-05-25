package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class AreSearchablesInitialisedTest extends AbstractTest {

	private AreSearchablesInitialised searchablesInitialised;

	@Before
	public void before() {
		searchablesInitialised = new AreSearchablesInitialised();
	}

	@Test
	public void evaluate() {
		boolean result = searchablesInitialised.evaluate(indexContext);
		assertTrue(result);

		when(indexContext.getMultiSearcher()).thenReturn(null);
		result = searchablesInitialised.evaluate(indexContext);
		assertFalse(result);

		result = searchablesInitialised.evaluate(indexContext);
		assertFalse(result);

		when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);
		result = searchablesInitialised.evaluate(indexContext);
		assertTrue(result);
	}

}
