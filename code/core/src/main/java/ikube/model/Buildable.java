package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This class represents configuration and properties, and potentially logic that can build another object. For example the analyzers may need input in the form
 * of files, then this class will hold the properties that are necessary for the analyzer to be instanciated, initialized and trained.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Buildable extends Persistable {

	private String filterType;
	private String analyzerType;
	private String algorithmType;

	private String trainingFilePath;

	private boolean log;
	private boolean compressed;

	public String getFilterType() {
		return filterType;
	}

	public void setFilterType(String filterType) {
		this.filterType = filterType;
	}

	public String getAnalyzerType() {
		return analyzerType;
	}

	public void setAnalyzerType(String analyzerType) {
		this.analyzerType = analyzerType;
	}

	public String getAlgorithmType() {
		return algorithmType;
	}

	public void setAlgorithmType(String algorithmType) {
		this.algorithmType = algorithmType;
	}

	public String getTrainingFilePath() {
		return trainingFilePath;
	}

	public void setTrainingFilePath(String trainingFilePath) {
		this.trainingFilePath = trainingFilePath;
	}

	public boolean isLog() {
		return log;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

}