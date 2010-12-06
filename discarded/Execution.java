package ikube.model;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 22.11.10
 * @version 01.00
 */
@Entity()
public class Execution extends Persistable {

	private String name;
	private int executions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getExecutions() {
		return executions;
	}

	public void setExecutions(int executions) {
		this.executions = executions;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(getName());
		builder.append(",");
		builder.append(getExecutions());
		builder.append("]");
		return builder.toString();
	}

}
