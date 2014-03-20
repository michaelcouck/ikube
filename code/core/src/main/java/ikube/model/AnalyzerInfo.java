package ikube.model;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This class contains class details to construct an analyzer, including the name of the analyzer
 * wrapper, the name of the underlyinb algorithm and the name of the filter class.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 14-03-2014
 */
@Entity
@Embeddable
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class AnalyzerInfo extends Persistable {

    /**
     * The analyzer wrapper in the system.
     */
    private String analyzer;
    /**
     * The underlying algorithm, probably Weka.
     */
    private String algorithm;
    /**
     * The data filter to transform the data into the required type if necessary. Can be null.
     */
    private String filter;

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
