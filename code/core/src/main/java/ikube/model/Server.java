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
	/** The port that the search web service is published to. */
	private int searchWebServicePort;
	/** The port that the monitoring web service is published to. */
	private int monitoringWebServicePort;
	/** The address of this machine. */
	private String address;
	/** The actions of this server. */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	private List<Action> actions;
	/** The age of this server. */
	private long age;

	private String searchWebServiceUrl;
	
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

	public void setActions(List<Action> actions) {
		this.actions = actions;
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

	public int getSearchWebServicePort() {
		return searchWebServicePort;
	}

	public void setSearchWebServicePort(int searchWebServicePort) {
		this.searchWebServicePort = searchWebServicePort;
	}

	public String getSearchWebServiceUrl() {
		return searchWebServiceUrl;
	}

	public void setSearchWebServiceUrl(String searchWebServiceUrl) {
		this.searchWebServiceUrl = searchWebServiceUrl;
	}

	public int getMonitoringWebServicePort() {
		return monitoringWebServicePort;
	}

	public void setMonitoringWebServicePort(int monitoringWebServicePort) {
		this.monitoringWebServicePort = monitoringWebServicePort;
	}

	public long getAge() {
		return age;
	}

	public void setAge(long age) {
		this.age = age;
	}

	public boolean equals(Object object) {
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
		return (int) (long) this.getId();
	}

	@Override
	public int compareTo(final Server other) {
		return this.getAddress().compareTo(other.getAddress());
	}

}
