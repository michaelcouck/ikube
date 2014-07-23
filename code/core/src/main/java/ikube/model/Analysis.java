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
 * @since 10-04-2013
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Analysis<Input, Output> extends Distributed {

    /**
     * The name of the analyzer in the system, for example clusterer-em. This corresponds to
     * the {@link ikube.model.Context} name, so if the context name in the Spring configuration is
     * sentiment-smo-en then the analysis analyzer field name must be the same. Note also that this field
     * and the context field must be the same as the Spring bean id/name too.
     */
    private String context;
    /**
     * The class/cluster for the instance, this is the result of the analysis.
     */
    private String clazz;
    /**
     * The input data to be analyzed, string text, or an array of dates and numbers, whatever, but typically
     * in the underlying function format. In the case of Weka it is the Weka format of course.
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
     * The time taken for the analysis.
     */
    private double duration;
    /**
     * The size of a particular cluster or class in the case of a classifier.
     */
    private int sizeForClassOfCluster;

    @Transient
    private transient Exception exception;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
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

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getSizeForClassOfCluster() {
        return sizeForClassOfCluster;
    }

    public void setSizeForClassOrCluster(int sizeForClassOfCluster) {
        this.sizeForClassOfCluster = sizeForClassOfCluster;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}