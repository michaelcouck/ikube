package ikube.model;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;

import org.jgroups.Address;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class Token extends Persistable {

	/** The address of the server that has the token. */
	private Address address;
	/** The time the token was sent. */
	private long start;
	/**
	 * The current list of servers. This list includes ourselves but we keep the local copy of this server, i.e. we don't take all the
	 * servers that are in the token. The token on this server is always local, we just populate it with the servers in the token that is
	 * sent to us.
	 */
	private Set<Server> servers;

	public Token() {
		this.servers = new TreeSet<Server>();
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

	public void setServer(Server server) {
		this.servers.add(server);
	}

	public Set<Server> getServers() {
		return servers;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("[").append(getId()).append(", ").append(getAddress()).append(", ").append(getStart())
				.append(", ").append(getServers()).append("]");
		return builder.toString();
	}

}
