package ikube.analytics.toolkit;

import ikube.AbstractTest;
import org.junit.Before;
import org.junit.Test;

public class CorrelationTest extends AbstractTest {

	private Correlation correlation;

	@Before
	public void before() {
		correlation = new Correlation();
	}

	@Test
	public void correlate() {
		correlation.correlate();
	}

}
