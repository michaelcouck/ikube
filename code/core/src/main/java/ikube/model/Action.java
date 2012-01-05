package ikube.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { //
@NamedQuery(name = Action.SELECT_FROM_ACTIONS, query = Action.SELECT_FROM_ACTIONS),
		@NamedQuery(name = Action.SELECT_FROM_ACTIONS_COUNT, query = Action.SELECT_FROM_ACTIONS_COUNT),
		@NamedQuery(name = Action.SELECT_FROM_ACTIONS_BY_NAME_COUNT, query = Action.SELECT_FROM_ACTIONS_BY_NAME_COUNT),
		@NamedQuery(name = Action.SELECT_FROM_ACTIONS_BY_NAME_DESC, query = Action.SELECT_FROM_ACTIONS_BY_NAME_DESC),
		@NamedQuery(name = Action.SELECT_FROM_ACTIONS_BY_ACTION_NAME_INDEX_NAME_AND_WORKING, //
		query = Action.SELECT_FROM_ACTIONS_BY_ACTION_NAME_INDEX_NAME_AND_WORKING) })
public class Action extends Persistable {

	public static final String SELECT_FROM_ACTIONS_COUNT = "select count(a) from Action as a";
	public static final String SELECT_FROM_ACTIONS_BY_NAME_COUNT = "select count(a) from Action as a " //
			+ "where a.actionName = :actionName";
	public static final String SELECT_FROM_ACTIONS = "select a from Action as a " //
			+ "order by a.id asc";
	public static final String SELECT_FROM_ACTIONS_BY_NAME_DESC = "select a from Action as a " //
			+ "where a.actionName = :actionName " //
			+ "order by a.id desc";
	public static final String SELECT_FROM_ACTIONS_BY_ACTION_NAME_INDEX_NAME_AND_WORKING = "select a from Action as a " //
			+ "where a.actionName = :actionName " //
			+ "and a.indexName = :indexName " //
			+ "and a.endTime is null " //
			+ "order by a.startTime desc";

	/** The name of the action that is executing. */
	private String actionName;
	/** The currently executing indexable. */
	private String indexableName;
	/** The actionName of the currently executing index. */
	private String indexName;
	/** The time the action was started. */
	@Temporal(value = TemporalType.DATE)
	private Date startTime;
	/** The time the action ended. */
	@Temporal(value = TemporalType.DATE)
	private Date endTime;
	/** The time it took for this action to finish. */
	private long duration;
	/** The predicate for the rules. */
	private String ruleExpression;
	/** The result from the rules and the predicate. */
	private boolean result;
	/** The number of documents that were added during the execution of the action. */
	private int invocations;

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

	public String getRuleExpression() {
		return ruleExpression;
	}

	public void setRuleExpression(String ruleExpression) {
		this.ruleExpression = ruleExpression;
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

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, Boolean.FALSE);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, Boolean.FALSE);
	}

}
