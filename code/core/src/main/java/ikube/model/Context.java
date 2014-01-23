package ikube.model;

import ikube.analytics.IAnalyzer;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This class represents configuration and properties, and potentially logic that can build another object. For example the analyzers may need
 * input in the form of files, then this class will hold the properties that are necessary for the analyzer to be instanciated, initialized and
 * trained.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10.04.13
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Context<T, F, A> extends Persistable implements IAnalyzer.IContext<T, F, A> {

    /**
     * @see ikube.analytics.IAnalyzer.IContext
     */
    private String name;
    /**
     * @see ikube.analytics.IAnalyzer.IContext
     */
    private T analyzer;
    /**
     * @see ikube.analytics.IAnalyzer.IContext
     */
    private F filter;
    /**
     * @see ikube.analytics.IAnalyzer.IContext
     */
    private A algorithm;
    /**
     * @see ikube.analytics.IAnalyzer.IContext
     */
    private int maxTraining;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public F getFilter() {
        return filter;
    }

    public void setFilter(F filter) {
        this.filter = filter;
    }

    public T getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(T analyzer) {
        this.analyzer = analyzer;
    }

    public A getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(A algorithm) {
        this.algorithm = algorithm;
    }

    public int getMaxTraining() {
        return maxTraining;
    }

    public void setMaxTraining(int maxTraining) {
        this.maxTraining = maxTraining;
    }
}