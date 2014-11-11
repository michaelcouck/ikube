package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

/**
 * This class represents configuration and properties, and potentially logic that can build another object. For
 * example the analyzers may need input in the form of files, then this class will hold the properties that are
 * necessary for the analyzer to be instantiated, initialized and trained.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */

@Entity
@SuppressWarnings("unchecked")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Context extends Persistable {

    /**
     * The name of this specific analyzer. The name will also be used to find the initial training file,
     * and indeed persist the instances that were used to train this analyzer for further investigation and
     * modification. This field is correlated to the {@link ikube.model.Analysis} 'analyzer' field. Note that
     * this field and the analysis object field must be the same as the Spring id/name of the bean.
     */
    private String name;

    /**
     * Ths internal type, i.e. the {@link ikube.analytics.IAnalyzer} type. Could be for instance
     * a {@link ikube.analytics.weka.WekaClassifier}. This class then holds a reference to the logical implementations,
     * and in the case of a classifier this could be a {@link weka.classifiers.functions.SMO} function.
     * <p/>
     * This typically is only defined using the interface, if the analyzers are defined in Spring,then we know immediately
     * what the type will be of course.
     */
    @Transient
    private Object analyzer;

    /**
     * The filter type to convert the data into for example feature vectors.
     */
    @Transient
    private Object[] filters;

    /**
     * The underlying algorithms for the analyzers, for example KMeans or J48 for example.
     */
    @Transient
    private Object[] algorithms;

    /**
     * The model that will train the analyzer. Typically this model is used to build the classifiers
     * and or used in the clustering of the data int he clusterers.
     */
    @Transient
    private transient Object[] models;
    /**
     * Any options or even classes that modify the algorithm in some way. Could be an array, something like a command line args.
     */
    @Transient
    private Object[] options;

    /**
     * Any options or even classes that modify the algorithm in some way. Could be an array, something like a command line args.
     */
    @Transient
    private Object[] capabilities;

    /**
     * The name of the file if different from the name of the algorithm.
     */
    @Transient
    private String[] fileNames;

    /**
     * If this is built using the rest api, then the files may not exist for the analyzers, in that
     * case the training data will be used for the training.
     */
    @Transient
    private String[] trainingDatas;

    /**
     * Ths maximum number of instances that can be used to train this analyzer.
     */
    @Transient
    private int[] maxTrainings;

    /**
     * The evaluation of the clusterer or classifier.
     */
    @Transient
    private String[] evaluations;

    /**
     * Whether this set of classifiers can be serialized and persisted on the file system.
     */
    private boolean persisted;

    /**
     * Whether the analyzer will build the models in parallel.
     */
    private boolean buildInParallel;

    /**
     * This flag is set when the model is built completely.
     */
    private boolean built;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Object analyzer) {
        this.analyzer = analyzer;
    }

    public Object[] getFilters() {
        return filters;
    }

    public void setFilters(Object... filters) {
        this.filters = filters;
    }

    public Object[] getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(Object... algorithms) {
        this.algorithms = algorithms;
    }

    public Object[] getModels() {
        return models;
    }

    public void setModels(Object... models) {
        this.models = models;
    }

    public Object[] getOptions() {
        return options;
    }

    public void setOptions(Object... options) {
        this.options = options;
    }

    public Object[] getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Object... capabilities) {
        this.capabilities = capabilities;
    }

    public int[] getMaxTrainings() {
        return maxTrainings;
    }

    public void setMaxTrainings(int... maxTrainings) {
        this.maxTrainings = maxTrainings;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    public void setFileNames(String... fileNames) {
        this.fileNames = fileNames;
    }

    public String[] getTrainingDatas() {
        return trainingDatas;
    }

    public void setTrainingDatas(String... trainingDatas) {
        this.trainingDatas = trainingDatas;
    }

    public String[] getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(String... evaluations) {
        this.evaluations = evaluations;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(boolean built) {
        this.built = built;
    }

    public boolean isBuildInParallel() {
        return buildInParallel;
    }

    public void setBuildInParallel(final boolean buildInParallel) {
        this.buildInParallel = buildInParallel;
    }
}