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
public class Analysis<I, O> extends Persistable {

	private String analyzer;

	private I input;
	private O output;
	private double duration;
	private Exception exception;
	@OneToOne
	@PrimaryKeyJoinColumn
	private Buildable buildable;

	public String getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}

	public I getInput() {
		return input;
	}

	public void setInput(I input) {
		this.input = input;
	}

	public O getOutput() {
		return output;
	}

	public void setOutput(O output) {
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

	public Buildable getBuildable() {
		return buildable;
	}

	public void setBuildable(Buildable buildable) {
		this.buildable = buildable;
	}

}