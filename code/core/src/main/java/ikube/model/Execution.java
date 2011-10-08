package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Michael Couck
 * @since 22.05.2011
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Execution extends Persistable {

	private String	name;
	private String	type;
	private int		invocations;
	private long	duration;
	private double	executionsPerSecond;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		return executionsPerSecond;
	}

	public void setExecutionsPerSecond(double executionsPerSecond) {
		this.executionsPerSecond = executionsPerSecond;
	}

}
