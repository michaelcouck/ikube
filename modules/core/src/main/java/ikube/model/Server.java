package ikube.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class Server extends Persistable implements Comparable<Server> {

	public class Action extends Persistable {

		/** The row id of the next row. */
		private long idNumber;
		/** The currently executing indexable. */
		private String indexableName;
		/** The actionName of the currently executing index. */
		private String indexName;
		/** The time the action was started. */
		private long startTime;

		public Action() {
		}

		public Action(long idNumber, String indexableName, String indexName, long startTime) {
			this.idNumber = idNumber;
			this.indexableName = indexableName;
			this.indexName = indexName;
			this.startTime = startTime;
		}

		public long getIdNumber() {
			return idNumber;
		}

		public void setIdNumber(long idNumber) {
			this.idNumber = idNumber;
		}

		public String getIndexableName() {
			return indexableName;
		}

		public void setIndexableName(String indexableName) {
			this.indexableName = indexableName;
		}

		public String getIndexName() {
			return indexName;
		}

		public void setIndexName(String indexName) {
			this.indexName = indexName;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		public String toString() {
			final StringBuilder builder = new StringBuilder("[");
			builder.append(getId());
			builder.append(", ");
			builder.append(getStartTime());
			builder.append(", ");
			builder.append(getIndexName());
			builder.append("]");
			return builder.toString();
		}

	}

	/** The address of this machine. */
	private String address;
	/** Whether this server is working. */
	private boolean working;
	/** The details about the action that this server is executing. */
	private List<Action> actions;

	public Server() {
		this.actions = new ArrayList<Server.Action>();
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isWorking() {
		return working;
	}

	public void setWorking(boolean working) {
		this.working = working;
	}

	public List<Action> getActions() {
		return actions;
	}

	@Override
	public int compareTo(Server o) {
		return this.getAddress().compareTo(o.getAddress());
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(", ").append(getAddress());
		builder.append("]");
		return builder.toString();
	}

}
