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
public class Server extends Persistable implements Comparable<Server> {

	/** The real ip address of this server. */
	private String ip;
	/** The JGroups address of this machine. */
	private Address address;

	/**
	 * The contexts in this server. These get passed around in the token => server => contexts, but this copy of the contexts are 'local',
	 * i.e. we don't get them from the other servers. So they have all local data. We use the data in the other servers => contexts to
	 * update this server.
	 */
	private Set<IndexContext> indexContexts;

	public Server() {
		this.indexContexts = new TreeSet<IndexContext>();
	}

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

	public Set<IndexContext> getIndexContexts() {
		return indexContexts;
	}

	public void setIndexContexts(Set<IndexContext> indexContexts) {
		this.indexContexts = indexContexts;
	}

	@Override
	public int compareTo(Server o) {
		return o.getAddress().compareTo(getAddress());
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(",").append(getIp()).append(", ").append(getAddress());
		builder.append("]");
		return builder.toString();
	}

}
