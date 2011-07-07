package ikube.model;

import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

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
	/** The details about the action that this server is executing. */
	private Action action;
	/** The search web service url for this server. */
	private String searchWebServiceUrl;
	/** The monitoring service for this server. */
	private String monitoringWebServiceUrl;
	/** The list of web service urls. */
	private final List<String> webServiceUrls;
	/** The age of this server. */
	private long age;
	/** The performance monitoring data. */
	private Map<String, Execution> indexingExecutions;
	private Map<String, Execution> searchingExecutions;

	public Server() {
		super();
		this.webServiceUrls = new ArrayList<String>();
		this.searchingExecutions = new HashMap<String, Execution>();
		this.indexingExecutions = new HashMap<String, Execution>();
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

	public boolean getWorking() {
		return this.action != null ? this.action.getWorking() : Boolean.FALSE;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public List<String> getWebServiceUrls() {
		return webServiceUrls;
	}

	public String getSearchWebServiceUrl() {
		if (this.searchWebServiceUrl == null) {
			for (String webServiceUrl : webServiceUrls) {
				if (webServiceUrl.contains(ISearcherWebService.class.getSimpleName())) {
					this.searchWebServiceUrl = webServiceUrl;
					break;
				}
			}
		}
		return this.searchWebServiceUrl;
	}

	public String getMonitoringWebServiceUrl() {
		if (this.monitoringWebServiceUrl == null) {
			for (String webServiceUrl : webServiceUrls) {
				if (webServiceUrl.contains(IMonitorWebService.class.getSimpleName())) {
					this.monitoringWebServiceUrl = webServiceUrl;
					break;
				}
			}
		}
		return this.monitoringWebServiceUrl;
	}

	public long getAge() {
		return age;
	}

	public void setAge(long age) {
		this.age = age;
	}

	public Map<String, Execution> getSearchingExecutions() {
		return searchingExecutions;
	}

	public void setSearchingExecutions(Map<String, Execution> searchingExecutions) {
		this.searchingExecutions = searchingExecutions;
	}

	public Map<String, Execution> getIndexingExecutions() {
		return indexingExecutions;
	}

	public void setIndexingExecutions(Map<String, Execution> indexingExecutions) {
		this.indexingExecutions = indexingExecutions;
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
		if (this.getId() == null) {
			return super.hashCode();
		}
		return this.getId().intValue();
	}

	@Override
	public int compareTo(final Server other) {
		return this.getAddress().compareTo(other.getAddress());
	}

}
