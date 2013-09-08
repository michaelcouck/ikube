package ikube.analytics;

import org.junit.Test;

public class WekaClassifierTest {

	@Test
	public void classify() throws Exception {
		String text = "training text";
		WekaClassifier wekaClassifier = new WekaClassifier();
		wekaClassifier.train(text);
		wekaClassifier.classify("positive");
		wekaClassifier.classify("negative");
	}

}
