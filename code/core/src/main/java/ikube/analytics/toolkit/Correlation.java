package ikube.analytics.toolkit;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
public class Correlation {

    public void correlate() {
        double[][] data = {{1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}};
        @SuppressWarnings("UnusedDeclaration")
        RealMatrix covarienceMatrix = correlate(data);
    }

    public RealMatrix correlate(double[][] data) {
        RealMatrix realMatrix = new BlockRealMatrix(data);
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(realMatrix);
        @SuppressWarnings("UnusedDeclaration")
        double correlation = pearsonsCorrelation.getCorrelationPValues().getEntry(0, 1);

        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
        @SuppressWarnings("UnusedDeclaration")
        RealMatrix resultMatrix = spearmansCorrelation.computeCorrelationMatrix(realMatrix);

        Covariance covariance = new Covariance(realMatrix);
        @SuppressWarnings("UnnecessaryLocalVariable")
        RealMatrix covarienceMatrix = covariance.getCovarianceMatrix();

        return covarienceMatrix;
    }

}
