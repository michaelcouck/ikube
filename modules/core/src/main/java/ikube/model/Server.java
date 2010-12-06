package ikube.model;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class Server extends Persistable {

	/** The time the action was started. */
	private long start;
	/** The name of the currently executing index. */
	private String indexName;
	/** The name of the action that is being executed on this configuration. */
	private String action;
	/** Whether this server is working. */
	private boolean working;
	/** The address of this machine. */
	private String address;

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isWorking() {
		return working;
	}

	public void setWorking(boolean working) {
		this.working = working;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(", ").append(getAddress());
		builder.append("]");
		return builder.toString();
	}

}
