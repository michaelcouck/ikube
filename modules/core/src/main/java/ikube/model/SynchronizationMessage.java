package ikube.model;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 18.12.10
 * @version 01.00
 */
@Entity()
public class SynchronizationMessage extends Persistable {

	private String ip;
	private Integer port;
	private String filePath;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId());
		builder.append(", ").append(getIp());
		builder.append(", ").append(getFilePath());
		builder.append("]");
		return builder.toString();
	}

}