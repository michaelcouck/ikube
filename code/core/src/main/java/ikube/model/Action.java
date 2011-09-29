package ikube.model;

import ikube.IConstants;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { @NamedQuery(name = Action.SELECT_FROM_ACTIONS, query = Action.SELECT_FROM_ACTIONS),
		@NamedQuery(name = Action.SELECT_FROM_ACTIONS_COUNT, query = Action.SELECT_FROM_ACTIONS_COUNT) })
public class Action extends Persistable {

	public static final String	SELECT_FROM_ACTIONS			= "select a from Action as a order by a.id asc";
	public static final String	SELECT_FROM_ACTIONS_COUNT	= "select count(a) from Action as a";

	/** The row id of the next row. */
	private long				idNumber;
	/** The name of the action that is executing. */
	private String				actionName;
	/** The name of the server that executes this action. */
	private String				serverName;
	/** The currently executing indexable. */
	private String				indexableName;
	/** The actionName of the currently executing index. */
	private String				indexName;
	/** The time the action was started. */
	@Temporal(value = TemporalType.TIMESTAMP)
	private Timestamp			startTime;
	/** The time the action ended. */
	@Temporal(value = TemporalType.TIMESTAMP)
	private Timestamp			endTime;
	/** The time it took for this action to finish. */
	private long				duration;
	/** Whether this server is working. */
	private boolean				working;
	/** The predicate for the rules. */
	private String				ruleExpression;
	/** The rules that were evaluated for this action. */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "action", fetch = FetchType.EAGER)
	private List<Rule>			rules;
	/** The result from the rules and the predicate. */
	private boolean				result;

	/**
	 * Default constructor.
	 */
	public Action() {}

	public Action(final long idNumber, final String actionName, final String indexableName, final String indexName, final Timestamp startTime,
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

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
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

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(final Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(final Timestamp endTime) {
		this.endTime = endTime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean getWorking() {
		return working;
	}

	public void setWorking(final boolean working) {
		this.working = working;
	}

	public String getStartDate() {
		return IConstants.HHMMSS_DDMMYYYY.format(new Date(this.startTime.getTime()));
	}

	public String getRuleExpression() {
		return ruleExpression;
	}

	public void setRuleExpression(String ruleExpression) {
		this.ruleExpression = ruleExpression;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
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
