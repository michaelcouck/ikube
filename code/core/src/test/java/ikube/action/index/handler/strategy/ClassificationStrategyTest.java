package ikube.action.index.handler.strategy;

import ikube.AbstractTest;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.LogisticRegression;
import com.aliasi.stats.RegressionPrior;

public class ClassificationStrategyTest extends AbstractTest {

	// parallel to inputs
	public static final int[] OUTPUTS = new int[] { 1, 1, 2, 2, 0, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 0, 1, 1, 2, 2, 2, 2, 1, 1, 0, 2, 2, 2, 2, 0, 2, 2, 2,
			2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 0, 2, 2, 0, 2, 1, 0, 0, 2, 2, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1,
			2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 0, 0, 1, 0, 1, 0, 1, 0, 2, 2, 1, 2, 0, 2, 1, 2, 2, 1, 2, 2, 0, 1, 1, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 1, 2,
			1, 2, 2, 0, 2, 2, 2, 2, 1, 2, 1, 2, 1, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 0, 2, 1, 0, 1, 2, 1, 2, 1, 1, 0, 1, 1, 0, 1, 1, 2, 2, 1, 0, 1, 2,
			1, 2, 0, 1, 2, 1, 2, 2, 2, 2, 2, 1, };

	// parallel to outputs
	public static final Vector[] INPUTS = new Vector[] { new DenseVector(new double[] { 1, 0, 0, 2, 0 }), new DenseVector(new double[] { 1, 0, 0, 2, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 2, 0 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 2, 1 }), new DenseVector(new double[] { 1, 0, 1, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }), new DenseVector(new double[] { 1, 0, 0, 2, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 3, 0 }), new DenseVector(new double[] { 1, 1, 1, 3, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 2, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 1, 1, 0 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 1, 0, 3, 0 }), new DenseVector(new double[] { 1, 1, 0, 2, 0 }), new DenseVector(new double[] { 1, 1, 0, 2, 0 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 2, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 2, 0 }), new DenseVector(new double[] { 1, 1, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 1, 1, 2, 1 }), new DenseVector(new double[] { 1, 0, 0, 2, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 2, 0 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 0 }), new DenseVector(new double[] { 1, 0, 1, 3, 0 }), new DenseVector(new double[] { 1, 1, 0, 2, 0 }),
			new DenseVector(new double[] { 1, 0, 0, 2, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 0 }), new DenseVector(new double[] { 1, 1, 1, 1, 0 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 0 }), new DenseVector(new double[] { 1, 1, 1, 3, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 3, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 0 }), new DenseVector(new double[] { 1, 1, 0, 3, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 3, 1 }), new DenseVector(new double[] { 1, 1, 1, 2, 1 }), new DenseVector(new double[] { 1, 1, 0, 2, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 3, 0 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 2, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 0 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 2, 1 }),
			new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 2, 0 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 3, 0 }),
			new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 3, 1 }), new DenseVector(new double[] { 1, 0, 0, 3, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 1, 3, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 3, 0 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 3, 0 }),
			new DenseVector(new double[] { 1, 0, 1, 2, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 2, 0 }), new DenseVector(new double[] { 1, 1, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 0 }), new DenseVector(new double[] { 1, 0, 0, 2, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 3, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 0 }), new DenseVector(new double[] { 1, 0, 1, 1, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 2, 0 }),
			new DenseVector(new double[] { 1, 1, 1, 2, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 0 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 0 }),
			new DenseVector(new double[] { 1, 0, 1, 2, 1 }), new DenseVector(new double[] { 1, 1, 1, 2, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 3, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 0 }), new DenseVector(new double[] { 1, 1, 0, 3, 0 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 2, 0 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 2, 0 }), new DenseVector(new double[] { 1, 1, 0, 2, 0 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 1, 3, 0 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 2, 0 }), new DenseVector(new double[] { 1, 0, 1, 2, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 2, 0 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 0, 1, 2, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 3, 0 }), new DenseVector(new double[] { 1, 1, 1, 1, 0 }), new DenseVector(new double[] { 1, 0, 0, 3, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 2, 1 }), new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 3, 1 }),
			new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 1, 1, 1, 1 }), new DenseVector(new double[] { 1, 1, 0, 1, 1 }),
			new DenseVector(new double[] { 1, 1, 0, 1, 1 }), };

	public static final Vector[] TEST_INPUTS = new Vector[] { new DenseVector(new double[] { 1, 0, 0, 1, 1 }), new DenseVector(new double[] { 1, 0, 1, 0, 0 }),
			new DenseVector(new double[] { 1, 0, 1, 3, 1 }), };

	@Test
	@Ignore
	public void classify() throws IOException {
		String context = "abcdefghij";
		LogisticRegression regression = //
		LogisticRegression.estimate(//
				INPUTS, //
				OUTPUTS, //
				RegressionPrior.noninformative(), //
				AnnealingSchedule.inverse(.05, 100), //
				null, // null reporter
				0.000000001, // min improve
				1, // min epochs
				10000); // max epochs
		LogisticRegressionClassifier logisticRegressionClassifier = null;
	}

}
