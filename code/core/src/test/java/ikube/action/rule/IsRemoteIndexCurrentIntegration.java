package ikube.action.rule;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 19-04-2014
 */
public class IsRemoteIndexCurrentIntegration extends AbstractTest {

	/**
	 * Class under test.
	 */
	private IsRemoteIndexCurrent isRemoteIndexCurrent;

	@Before
	public void before() {
		isRemoteIndexCurrent = ApplicationContextManager.getBean(IsRemoteIndexCurrent.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void evaluate() throws Exception {
		IndexContext indexContext = ApplicationContextManager.getBean(IConstants.GEOSPATIAL);
		boolean indexCurrent = isRemoteIndexCurrent.evaluate(indexContext);
		assertFalse("This index should never be current in the tests : ", indexCurrent);
	}

}