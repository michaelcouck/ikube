package ikube.action.index.handler.strategy;

import ikube.AbstractTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.stats.OnlineNormalEstimator;

public class ClassificationStrategyTest extends AbstractTest {

	@Test
	public void classify() throws IOException {
		String context = "abcdefghij";
		int ngram = 3;
		NGramProcessLM nGramProcessLM = new NGramProcessLM(ngram);
		OnlineNormalEstimator onlineNormalEstimator = new OnlineNormalEstimator();
		char[] chars = context.toCharArray();
		for (int n = 0; n < chars.length; n++) {
			double log2Prob = nGramProcessLM.log2ConditionalEstimate(chars, 0, n + 1);
			nGramProcessLM.trainConditional(chars, Math.max(0, n - ngram), n, Math.max(0, n - 1));
			onlineNormalEstimator.handle(log2Prob);
			double mean = onlineNormalEstimator.mean();
			double standardDeviation = onlineNormalEstimator.standardDeviationUnbiased();
			logger.debug("Log 2 probability : " + log2Prob + ", mean : " + mean + ", deviation : " + standardDeviation);
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		nGramProcessLM.writeTo(new ObjectOutputStream(byteArrayOutputStream));
		
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		NGramProcessLM nGramProcessLMClone = NGramProcessLM.readFrom(byteArrayInputStream);
		
		double probability = nGramProcessLM.prob("a");
		logger.info("Probability : " + probability + ", " + onlineNormalEstimator.toString());
		probability = nGramProcessLM.prob("e");
		logger.info("Probability : " + probability + ", " + onlineNormalEstimator.toString());
	}

}
