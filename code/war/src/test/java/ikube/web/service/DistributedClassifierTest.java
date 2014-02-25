package ikube.web.service;

import ikube.analytics.weka.WekaClassifier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import weka.classifiers.functions.SMO;

/**
 * This test is to see if the classifiers can be distributed in a cluster, and then the results
 * merged based on a probability or a distribution, perhaps a Euclidean distance from the vector
 * to a particular class.
 * <p/>
 * NOTE: You can not distribute the classifiers over several machines
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-02-2014
 */
@Ignore
public class DistributedClassifierTest extends DistributedTest {

    @Before
    public void before() {
        // Take text and build a classifier
        // Predict each instance and split according to class
        // Build two classifiers with the split data
        // Predict each instance with the new classifiers
        // Compare the prediction using the full data set to the partitioned data sets
    }

    @Test
    @SuppressWarnings("unchecked")
    public void partition() throws Exception {
        String name = "sentiment-tr-classifier";
        String header = "@relation training_data\n@attribute @class@ {positive,negative}\n@attribute @text@ string\n@data";
        Class<?>[] algorithms = {SMO.class};
        for (final Class<?> algorithm : algorithms) {
            partitionWith(name, WekaClassifier.class, algorithm, header, 1);
        }
    }

    /*WAODE, NaiveBayesUpdateable, NaiveBayesSimple, NaiveBayesMultinomialUpdateable, NaiveBayesMultinomial, NaiveBayes,
    HNB, DMNBtext, ComplementNaiveBayes, BayesNet, Winnow, VotedPerceptron, SPegasos, SMOreg, SMO, SimpleLogistic, SimpleLinearRegression,
    RBFNetwork, PLSClassifier, PaceRegression, LWL, LBR, KStar, IBk, IB1, ND, DataNearBalancedND, ClassBalancedND, Vote, ThresholdSelector,
    StackingC, Stacking, RotationForest, RegressionByDiscretization, RandomSubSpace, RandomCommittee, RacedIncrementalLogitBoost, OrdinalClassClassifier,
    SimpleMI, MIWrapper, MISVM, MISMO, MIOptimalBall, MINND, MILR, MIEMDD, MIDD, HyperPipes, SerializedClassifier, VFI, Regression, NeuralNetwork,
    GeneralRegression, ZeroR, Ridor, Prism, PART, OneR, NNge, M5Rules, JRip, DecisionTable, ConjunctiveRule*/

}