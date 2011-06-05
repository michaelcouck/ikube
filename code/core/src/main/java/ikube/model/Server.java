package ikube.model;

import ikube.IConstants;
import ikube.service.IMonitorWebService;
import ikube.service.ISearcherWebService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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

	public class Action implements Serializable {

		/** The row id of the next row. */
		private long idNumber;
		/** The currently executing indexable. */
		private String indexableName;
		/** The actionName of the currently executing index. */
		private String indexName;
		/** The time the action was started. */
		private long startTime;

		/**
		 * Default constructor.
		 */
		public Action() {
		}

		public Action(final long idNumber, final String indexableName, final String indexName, final long startTime) {
			this.idNumber = idNumber;
			this.indexableName = indexableName;
			this.indexName = indexName;
			this.startTime = startTime;
		}

		public long getIdNumber() {
			return idNumber;
		}

		public void setIdNumber(final long idNumber) {
			this.idNumber = idNumber;
		}

		public String getIndexableName() {
			return indexableName;
		}

		public void setIndexableName(final String indexableName) {
			this.indexableName = indexableName;
		}

		public String getIndexName() {
			return indexName;
		}

		public void setIndexName(final String indexName) {
			this.indexName = indexName;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(final long startTime) {
			this.startTime = startTime;
		}

		public String getStartDate() {
			return IConstants.HHMMSS_DDMMYYYY.format(new Date(this.startTime));
		}

		public String toString() {
			final StringBuilder builder = new StringBuilder("[");
			builder.append(getIndexableName());
			builder.append(", ");
			builder.append(getIndexName());
			builder.append(", ");
			builder.append(getStartTime());
			builder.append("]");
			return builder.toString();
		}

	}

	/** The ip of the server. */
	private String ip;
	/** The address of this machine. */
	private String address;
	/** Whether this server is working. */
	private boolean working;
	/** The details about the action that this server is executing. */
	private final List<Action> actions;
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
	/** The last 5 kilobytes of the log file for this server. */
	private String logTail;

	public Server() {
		super();
		this.actions = new ArrayList<Server.Action>();
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

	public void setAddress(final String address) {
		this.address = address;
	}

	public boolean getWorking() {
		return working;
	}

	public void setWorking(final boolean working) {
		this.working = working;
	}

	public List<Action> getActions() {
		return actions;
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

	public String getLogTail() {
		return logTail;
	}

	public void setLogTail(String logTail) {
		this.logTail = logTail;
	}

	public boolean equals(Object object) {
		if (object == null) {
			return Boolean.FALSE;
		}
		if (!this.getClass().isAssignableFrom(object.getClass())) {
			return Boolean.FALSE;
		}
		return this.getAddress().equals(((Server) object).getAddress());
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

	public String toString() {
		String logTailBackup = logTail;
		try {
			// Remove the log tail as it can be very long
			if (logTail != null) {
				logTail = null;
			}
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		} finally {
			logTail = logTailBackup;
		}
	}

}
