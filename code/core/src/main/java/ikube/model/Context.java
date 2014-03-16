package ikube.model;

import javax.persistence.*;

/**
 * This class represents configuration and properties, and potentially logic that can build another object. For
 * example the analyzers may need input in the form of files, then this class will hold the properties that are
 * necessary for the analyzer to be instantiated, initialized and trained.
 *
 * @param <T> the type of analyzer in Ikube system
 * @param <F> the type of the filter to convert the data to the input format
 * @param <A> the logical implementation or algorithm for the analyzer
 * @param <O> the possible options passed to the algorithm logic
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Context<T, F, A, O> extends Persistable {

    /**
     * The name of this specific analyzer. The name will also be used to find the initial training file,
     * and indeed persist the instances that were used to train this analyzer for further investigation and
     * modification. This field is correlated to the {@link ikube.model.Analysis} 'analyzer' field. Note that
     * this field and the analysis object field must be the same as the Spring id/name of the bean.
     */
    private String name;

    /**
     * Ths internal type, i.e. the {@link ikube.analytics.IAnalyzer} type. Could be for instance
     * a {@link ikube.analytics.weka.WekaClassifier}. This class then holds a reference to the logical implementation,
     * and in the case of a classifier this could be a {@link weka.classifiers.functions.SMO} function.
     * <p/>
     * This typically is only defined using the interface, if the analyzers are defined in Spring,then we know immediately
     * what the type will be of course.
     */
    @Transient
    private transient T analyzer;

    /**
     * The filter type to convert the data into for example feature vectors.
     */
    @Transient
    private transient F filter;

    /**
     * The underlying algorithm for the analyzer, for example KMeans or J48 for example.
     */
    @Transient
    private transient A algorithm;

    /**
     * Any options or even classes that modify the algorithm in some way. Could be an array, something like a command line args.
     */
    private transient O options;

    /**
     * This is the string training data, typically set from the front end.
     */
    @Transient
    private transient String trainingData;

    /**
     * Ths maximum number of instances that can be used to train this analyzer.
     */
    private int maxTraining;

    /**
     * The information to construct the analyzer.
     *
     * @see ikube.model.AnalyzerInfo
     */
    @Embedded
    private AnalyzerInfo analyzerInfo;

    public Context() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public F getFilter() {
        return filter;
    }

    public void setFilter(final F filter) {
        this.filter = filter;
    }

    public T getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(final T analyzer) {
        this.analyzer = analyzer;
    }

    public A getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(final A algorithm) {
        this.algorithm = algorithm;
    }

    public O getOptions() {
        return options;
    }

    public void setOptions(O options) {
        this.options = options;
    }

    public String getTrainingData() {
        return trainingData;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setTrainingData(final String trainingData) {
        this.trainingData = trainingData;
    }

    public int getMaxTraining() {
        return maxTraining;
    }

    public void setMaxTraining(final int maxTraining) {
        this.maxTraining = maxTraining;
    }

    public AnalyzerInfo getAnalyzerInfo() {
        return analyzerInfo;
    }

    public void setAnalyzerInfo(AnalyzerInfo analyzerInfo) {
        this.analyzerInfo = analyzerInfo;
    }

    public String toString() {
        return "Name : " + name + ", analyzer : " + analyzer + ", filter : " + filter + ", algorithm : " + algorithm;
    }
}