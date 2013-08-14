package ikube.analytics;

import org.junit.Before;

import ikube.AbstractTest;

public class EncogSvmClassifierTest extends AbstractTest {
	
	private EncogSvmClassifier encogSvmClassifier;
	
	@Before
	public void before() {
		encogSvmClassifier = new EncogSvmClassifier();
	}
	
}
