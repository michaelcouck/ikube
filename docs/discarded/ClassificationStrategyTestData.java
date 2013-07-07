package ikube.action.index.handler.strategy;

import org.junit.Ignore;

import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Vector;

@Ignore
public class ClassificationStrategyTestData {

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

}
