package ikube.web.tag.spelling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

public class CheckerExtTest {

	private Logger logger = Logger.getLogger(CheckerExtTest.class.getName());

	private CheckerExt checkerExt = new CheckerExt();

	@Test
	public void checkWord() {
		String wrong = "wrongk";
		String correct = "wrongs";
		String corrected = checkerExt.checkWords(wrong);
		logger.log(Level.SEVERE, corrected);
		assertEquals(correct, corrected);
	}

	@Test
	public void checkPerformance() {
		double iterationsPerSecond = execute(new IPerform() {
			public void execute() {
				checkerExt.checkWords("michael");
			}

			public boolean log() {
				return true;
			}
		}, "Spelling checker:", 1000);
		assertTrue(iterationsPerSecond > 100);
		iterationsPerSecond = execute(new IPerform() {
			public void execute() {
				checkerExt.checkWords("couck");
			}

			public boolean log() {
				return true;
			}
		}, "Spelling checker:", 1000);
		assertTrue(iterationsPerSecond > 100);
	}

	public interface IPerform {
		public boolean log();

		public void execute();
	}

	public double execute(IPerform perform, String type, double iterations) {
		double start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			perform.execute();
		}
		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000d;
		double executionsPerSecond = (iterations / duration);
		if (perform.log()) {
			logger.log(Level.SEVERE, "Duration : " + duration + ", " + type + " per second : " + executionsPerSecond);
		}
		return executionsPerSecond;
	}

}
