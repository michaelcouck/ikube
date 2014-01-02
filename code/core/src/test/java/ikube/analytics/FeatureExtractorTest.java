package ikube.analytics;

import static junit.framework.Assert.assertEquals;
import ikube.AbstractTest;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
@Deprecated
public class FeatureExtractorTest extends AbstractTest {

	private FeatureExtractor featureExtractor;

	@Before
	public void before() {
		featureExtractor = new FeatureExtractor();
	}

	@Test
	public void extractFeatures() throws IOException {
		String text = "In this wonderful world, love is all you need";
		double[] vector = featureExtractor.extractFeatures(text, text);
		for (final double d : vector) {
			assertEquals(1.0, d);
		}

		vector = featureExtractor.extractFeatures("Lets go to another world");
		for (int i = 0; i < vector.length; i++) {
			if (i == 5) {
				assertEquals(1.0, vector[i]);
			} else {
				assertEquals(0.0, vector[i]);
			}
		}
	}

}
