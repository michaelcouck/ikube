package ikube.model;

import ikube.IConstants;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Action extends Persistable {

	/** The row id of the next row. */
	private long idNumber;
	/** The name of the action that is executing. */
	private String actionName;
	/** The currently executing indexable. */
	private String indexableName;
	/** The actionName of the currently executing index. */
	private String indexName;
	/** The time the action was started. */
	private long startTime;
	/** Whether this server is working. */
	private boolean working;

	/**
	 * Default constructor.
	 */
	public Action() {
	}

	public Action(final long idNumber, final String actionName, final String indexableName, final String indexName, final long startTime,
			final boolean working) {
		this.idNumber = idNumber;
		this.actionName = actionName;
		this.indexableName = indexableName;
		this.indexName = indexName;
		this.startTime = startTime;
		this.working = working;
	}

	public long getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(final long idNumber) {
		this.idNumber = idNumber;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getIndexableName() {
		return indexableName;
	}

	public void setIndexableName(final String indexableName) {
		this.indexableName = indexableName;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(final String indexName) {
		this.indexName = indexName;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(final long startTime) {
		this.startTime = startTime;
	}

	public boolean getWorking() {
		return working;
	}

	public void setWorking(final boolean working) {
		this.working = working;
	}

	public String getStartDate() {
		return IConstants.HHMMSS_DDMMYYYY.format(new Date(this.startTime));
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, Boolean.FALSE);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, Boolean.FALSE);
	}

}
