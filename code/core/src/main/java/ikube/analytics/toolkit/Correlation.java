package ikube.analytics.toolkit;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class Correlation {

	private static final Logger LOGGER = LoggerFactory.getLogger(Correlation.class);

	public void correlate() {
		double[][] data = { { 1, 1 }, { 2, 2 }, { 3, 3 }, { 4, 4 }, { 5, 5 } };
		RealMatrix covarienceMatrix = correlate(data);
		LOGGER.info("Matrix : " + covarienceMatrix);
	}

	public RealMatrix correlate(double[][] data) {
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

		return covarienceMatrix;
	}

}
