package ikube.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { @NamedQuery(name = Action.SELECT_FROM_ACTIONS_COUNT, query = Action.SELECT_FROM_ACTIONS_COUNT) })
public class Action extends Persistable {

	public static final String SELECT_FROM_ACTIONS_COUNT = "select count(a) from Action as a";

	/** The name of the action that is executing. */
	@Column
	private String actionName;
	/** The currently executing indexable. */
	@Column
	private String indexableName;
	/** The actionName of the currently executing index. */
	@Column
	private String indexName;
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
	/** The result from the rules and the predicate. */
	@Column
	private boolean result;
	/** The number of documents that were added during the execution of the action. */
	@Column
	private int invocations;

	@ManyToOne(cascade = { CascadeType.DETACH }, fetch = FetchType.EAGER)
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

	public int getInvocations() {
		return invocations;
	}

	public void setInvocations(int invocations) {
		this.invocations = invocations;
	}

	public long getInvocationsPerSecond() {
		long duration = (System.currentTimeMillis() - startTime.getTime()) / 1000;
		if (duration == 0) {
			return 0;
		}
		return (invocations / duration);
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
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
