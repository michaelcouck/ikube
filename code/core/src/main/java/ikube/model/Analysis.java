package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

/**
 * This class represents data that is to be analyzed as well as the results from the analysis if any.
 * 
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Analysis<Input, Output> extends Persistable {

	/** The name of the analyzer in the system, for example clusterer-em. */
	private String analyzer;

	/** The class/cluster for the instance. */
	private Object clazz;
	/** The input data, for training too. */
	private Input input;
	/** The output data, could be a string or a double array for distribution. */
	private Output output;
	private double duration;
	private Exception exception;
	private boolean compressed;

	@OneToOne
	@PrimaryKeyJoinColumn
	private Buildable buildable;

	public String getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}

	public Object getClazz() {
		return clazz;
	}

	public void setClazz(Object clazz) {
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

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	public Buildable getBuildable() {
		return buildable;
	}

	public void setBuildable(Buildable buildable) {
		this.buildable = buildable;
	}

}