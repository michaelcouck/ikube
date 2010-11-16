package ikube.model;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;

@Entity()
public class Token extends Persistable {

	/** The ip of the server that has the token. */
	private String ip;
	/** The time the token was accepted. */
	private long start;
	/** The current list of servers. */
	private Set<Server> servers;

	public Token() {
		this.servers = new TreeSet<Server>();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String serverIpAddress) {
		this.ip = serverIpAddress;
	}

	public long getStart() {
		return start;
	}

	public void setStart(final long start) {
		this.start = start;
	}

	public void setServer(Server server) {
		this.servers.add(server);
	}

	public Set<Server> getServers() {
		return servers;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("[").append(getId()).append(", ").append(getIp()).append(", ").append(getStart()).append(
				"]");
		return builder.toString();
	}

}
