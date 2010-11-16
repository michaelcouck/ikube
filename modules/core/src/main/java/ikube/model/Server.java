package ikube.model;

import org.jgroups.Address;

public class Server extends Persistable implements Comparable<Server> {

	private String ip;
	private Address address;
	/** The time the action was started. */
	private long start;
	/** The name of the index that this server is currently working on if any. */
	private String index;
	/** The name of the action that this server is currently working on if any. */
	private String action;
	/** The last id number that this server started with. */
	private long idNumber;
	/** Whether this server is working. */
	private boolean working;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public long getStart() {
		return start;
	}

	public void setStart(final long start) {
		this.start = start;
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

	public long getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(long idNumber) {
		this.idNumber = idNumber;
	}

	public boolean isWorking() {
		return working;
	}

	public void setWorking(final boolean indexing) {
		this.working = indexing;
	}

	@Override
	public int compareTo(Server o) {
		return o.getAddress().compareTo(getAddress());
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(", ").append(isWorking()).append(", ").append(getIndex()).append(", ").append(getAddress()).append(
				", ").append(getAction()).append(", ").append(getIdNumber()).append(", ").append(getStart());
		builder.append("]");
		return builder.toString();
	}

}
