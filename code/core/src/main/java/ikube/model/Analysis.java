package ikube.model;

import weka.classifiers.functions.SMO;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

/**
 * This class represents data that is to be analyzed as well as the results from the analysis if any.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Analysis<Input, Output> extends Persistable {

    /**
     * The name of the analyzer in the system, for example clusterer-em.
     */
    private String analyzer;
    /**
     * The class/cluster for the instance, this is the result of the analysis.
     */
    private String clazz;
    /**
     * The input data to be analyzed, string text, or an array of dates and numbers, whatever, but typically in the underlying
     * function format. In the case of Weka it is the Weka format of course.
     */
    private Input input;
    /**
     * The output data, could be a string or a double array for distribution.
     */
    private Output output;
    /**
     * An algorithm specific output, could be toString from the {@link SMO} function.
     */
    private String algorithmOutput;
    /**
     * The correlation co-efficients for the data set, matching the first instance against the next.
     */
    @Transient
    private double[] correlationCoefficients;
    /**
     * The distribution probabilities of the instance in the clusters/class categories.
     */
    @Transient
    private double[][] distributionForInstances;
    /**
     * The classes or clusters available for the classifier or clusterer.
     */
    @Transient
    private Object[] classesOrClusters;
    /**
     * The size of each class or cluster in the classifier or clusterer.
     */
    @Transient
    private int[] sizesForClassesOrClusters;

    private double duration;
    private boolean correlation;
    private boolean distribution;
    private boolean classesAndClusters;
    private boolean sizesForClassesAndClusters;

    private Exception exception;

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public String getAlgorithmOutput() {
        return algorithmOutput;
    }

    public void setAlgorithmOutput(String algorithmOutput) {
        this.algorithmOutput = algorithmOutput;
    }

    public double[] getCorrelationCoefficients() {
        return correlationCoefficients;
    }

    public void setCorrelationCoefficients(double[] correlationCoefficients) {
        this.correlationCoefficients = correlationCoefficients;
    }

    public double[][] getDistributionForInstances() {
        return distributionForInstances;
    }

    public void setDistributionForInstances(double[][] distributionForInstances) {
        this.distributionForInstances = distributionForInstances;
    }

    public Object[] getClassesOrClusters() {
        return classesOrClusters;
    }

    public void setClassesOrClusters(Object[] classesOrClusters) {
        this.classesOrClusters = classesOrClusters;
    }

    public int[] getSizesForClassesOrClusters() {
        return sizesForClassesOrClusters;
    }

    public void setSizesForClassesOrClusters(int[] sizesForClassesOrClusters) {
        this.sizesForClassesOrClusters = sizesForClassesOrClusters;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public boolean isCorrelation() {
        return correlation;
    }

    public void setCorrelation(boolean correlation) {
        this.correlation = correlation;
    }

    public boolean isDistribution() {
        return distribution;
    }

    public void setDistribution(boolean distribution) {
        this.distribution = distribution;
    }

    public boolean isClassesAndClusters() {
        return classesAndClusters;
    }

    public void setClassesAndClusters(boolean classesAndClusters) {
        this.classesAndClusters = classesAndClusters;
    }

    public boolean isSizesForClassesAndClusters() {
        return sizesForClassesAndClusters;
    }

    public void setSizesForClassesAndClusters(boolean sizesForClassesAndClusters) {
        this.sizesForClassesAndClusters = sizesForClassesAndClusters;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}