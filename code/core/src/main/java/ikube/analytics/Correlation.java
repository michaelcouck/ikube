package ikube.analytics;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

public class Correlation {

	public void correlate() {
		double[][] data = { { 1, 1 }, { 2, 2 }, { 3, 3 }, { 4, 4 }, { 5, 5 } };
		RealMatrix realMatrix = new BlockRealMatrix(data);
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(realMatrix);
		double correlation = pearsonsCorrelation.getCorrelationPValues().getEntry(0, 1);
		System.out.println("Correlation : " + correlation);

		SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
		RealMatrix resultMatrix = spearmansCorrelation.computeCorrelationMatrix(realMatrix);
		System.out.println("Correlation : " + resultMatrix);

		Covariance covariance = new Covariance(realMatrix);
		RealMatrix covarienceMatrix = covariance.getCovarianceMatrix();
		System.out.println("Correlation : " + covarienceMatrix);
	}

}
