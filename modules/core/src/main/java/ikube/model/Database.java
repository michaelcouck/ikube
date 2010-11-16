package ikube.model;

import java.sql.Timestamp;

import javax.persistence.Entity;

/**
 * @deprecated Model changed to cluster synchronisation
 * @author Michael Couck
 */
@Entity()
public class Database extends Persistable {

	private String ip;
	private int port;
	private transient Timestamp start;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Timestamp getStart() {
		return start;
	}

	public void setStart(Timestamp start) {
		this.start = start;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(", ").append(getIp()).append(", ").append(getStart());
		builder.append("]");
		return builder.toString();
	}

}
