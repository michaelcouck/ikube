package ikube.model;

import javax.persistence.Entity;

@Entity()
public class Server extends Persistable {

	/** The ip of this machine. */
	private String ip;
	/** The name of the index that this server is currently working on if any. */
	private String index;
	/** The name of the action that this server is currently working on if any. */
	private String action;
	/** The batch number of this server. */
	private int batch;
	/** The start time of this action. */
	private long start;
	/** Whether this server is working. */
	private boolean working;

	public String getIp() {
		return ip;
	}

	public void setIp(String serverIpAddress) {
		this.ip = serverIpAddress;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(final String indexName) {
		this.index = indexName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(final String actionName) {
		this.action = actionName;
	}

	public int getBatch() {
		return batch;
	}

	public void setBatch(int batch) {
		this.batch = batch;
	}

	public long getStart() {
		return start;
	}

	public void setStart(final long start) {
		this.start = start;
	}

	public boolean isWorking() {
		return working;
	}

	public void setWorking(final boolean indexing) {
		this.working = indexing;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(", ").append(isWorking()).append(", ").append(getIndex()).append(", ").append(getIp()).append(", ")
				.append(getAction()).append(", ").append(getBatch()).append(", ").append(getStart());
		builder.append("]");
		return builder.toString();
	}

}
