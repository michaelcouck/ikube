package ikube.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

/**
 * This object is passed around in the cluster.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Server extends Persistable implements Comparable<Server> {

	/** The ip of the server. */
	private String ip;
	/** The address of this machine. */
	private String address;
	/** The actions of this server. */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private List<Action> actions;
	@SuppressWarnings("rawtypes")
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private List<IndexContext> indexContexts;
	/** The age of this server. */
	private long age;

	public Server() {
		this.actions = new ArrayList<Action>();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(final String ip) {
		this.ip = ip;
	}

	public String getAddress() {
		return address;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(final List<Action> actions) {
		this.actions = actions;
	}

	@SuppressWarnings("rawtypes")
	public List<IndexContext> getIndexContexts() {
		return indexContexts;
	}

	@SuppressWarnings("rawtypes")
	public void setIndexContexts(List<IndexContext> indexContexts) {
		this.indexContexts = indexContexts;
	}

	public boolean isWorking() {
		if (actions != null) {
			for (Action action : actions) {
				if (action.getEndTime() == null) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public long getAge() {
		return age;
	}

	public void setAge(long age) {
		this.age = age;
	}

	public boolean equals(final Object object) {
		if (object == null) {
			return Boolean.FALSE;
		}
		if (!this.getClass().isAssignableFrom(object.getClass())) {
			return Boolean.FALSE;
		}
		return compareTo((Server) object) == 0;
	}

	public int hashCode() {
		if (this.getId() == 0) {
			return super.hashCode();
		}
		return (int) this.getId();
	}

	@Override
	public int compareTo(final Server other) {
		return this.getAddress().compareTo(other.getAddress());
	}

}