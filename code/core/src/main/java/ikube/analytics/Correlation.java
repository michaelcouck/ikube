package ikube.analytics;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.junit.Test;

public class Correlation {

	@Test
	public void main() {
		double[][] data = { { 1, 1 }, { 2, 2 }, { 3, 3 }, { 4, 4 }, { 5, 4 } };
		RealMatrix realMatrix = new BlockRealMatrix(data);
		double correlation = new PearsonsCorrelation(realMatrix).getCorrelationPValues().getEntry(0, 1);
		System.out.println("Correlation : " + correlation);
	}

}
