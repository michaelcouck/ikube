package ikube.model;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity()
public class Server extends Persistable {

	private String indexName;
	private String serverName;
	private String serverIpAddress;
	private String actionName;
	private Timestamp start;
	private boolean indexing;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(final String indexName) {
		this.indexName = indexName;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(final String serverName) {
		this.serverName = serverName;
	}

	public String getServerIpAddress() {
		return serverIpAddress;
	}

	public void setServerIpAddress(String serverIpAddress) {
		this.serverIpAddress = serverIpAddress;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(final String actionName) {
		this.actionName = actionName;
	}

	public Timestamp getStart() {
		return start;
	}

	public void setStart(final Timestamp start) {
		this.start = start;
	}

	public boolean isWorking() {
		return indexing;
	}

	public void setWorking(final boolean indexing) {
		this.indexing = indexing;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(", ").append(isWorking()).append(", ").append(getIndexName()).append(", ").append(getServerName())
				.append(", ").append(getServerIpAddress()).append(", ").append(getActionName()).append(", ").append(getStart());
		builder.append("]");
		return builder.toString();
	}

}
