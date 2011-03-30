package ikube.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

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
	/** The list of web service urls. */
	private final List<String> webServiceUrls;

	public Server() {
		super();
		this.actions = new ArrayList<Server.Action>();
		this.webServiceUrls = new ArrayList<String>();
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

	public boolean isWorking() {
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

	@Override
	public int compareTo(final Server other) {
		return this.getAddress().compareTo(other.getAddress());
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
