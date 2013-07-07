package ikube.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Action extends Persistable {

	/** The name of the 'real' action that is executing. */
	@Column
	private String actionName;
	/** The name of the currently executing index. */
	@Column
	private String indexName;
	/** The currently executing indexable. */
	@Column
	private String indexableName;
	/** The time the action was started. */
	@Column
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date startTime;
	/** The time the action ended. */
	@Column
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date endTime;
	/** The time it took for this action to finish. */
	@Column
	private long duration;
	/** The category from the rules and the predicate. */
	@Column
	private boolean result;
	/** The latest snapshot for the index context. */
	@OneToOne
	@PrimaryKeyJoinColumn
	private Snapshot snapshot;
	/** The server object where the actions is executing. */
	@Transient
	private Server server;

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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(final Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(final Date endTime) {
		this.endTime = endTime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, Boolean.FALSE);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, Boolean.FALSE);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE, false);
	}

}
