package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.ATest;

import org.junit.Before;
import org.junit.Test;

/**
 * TODO Implement me!
 * 
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class AreSearchablesInitialisedTest extends ATest {

	private AreSearchablesInitialised searchablesInitialised;

	public AreSearchablesInitialisedTest() {
		super(AreSearchablesInitialisedTest.class);
	}

	@Before
	public void before() {
		searchablesInitialised = new AreSearchablesInitialised();
	}

	@Test
	public void evaluate() {
		boolean result = searchablesInitialised.evaluate(indexContext);
		assertTrue(result);

		when(indexContext.getIndex()).thenReturn(null);
		result = searchablesInitialised.evaluate(indexContext);
		assertFalse(result);

		when(indexContext.getIndex()).thenReturn(index);
		when(indexContext.getIndex().getMultiSearcher()).thenReturn(null);
		result = searchablesInitialised.evaluate(indexContext);
		assertFalse(result);

		when(indexContext.getIndex().getMultiSearcher()).thenReturn(multiSearcher);
	}

}
