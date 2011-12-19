package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author Michael Couck
 * @since 22.05.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { 
		@NamedQuery(
				name = Execution.SELECT_FROM_EXECUTIONS_BY_NAME_TYPE, 
				query = Execution.SELECT_FROM_EXECUTIONS_BY_NAME_TYPE) })
public class Execution extends Persistable {

	public static final String SELECT_FROM_EXECUTIONS_BY_NAME_TYPE = //
		"select e from Execution as e where e.indexName = :indexName and e.type = :type";

	private String type;
	private long duration;
	private int invocations;
	private String indexName;
	private double executionsPerSecond;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String name) {
		this.indexName = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getInvocations() {
		return invocations;
	}

	public void setInvocations(int invocations) {
		this.invocations = invocations;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public double getExecutionsPerSecond() {
		if (duration > 0) {
			setExecutionsPerSecond(invocations / duration);
		}
		return executionsPerSecond;
	}

	public void setExecutionsPerSecond(double executionsPerSecond) {
		this.executionsPerSecond = executionsPerSecond;
	}

}
