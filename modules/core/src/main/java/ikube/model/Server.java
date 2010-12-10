package ikube.model;

import javax.persistence.Entity;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class Server extends Persistable implements Comparable<Server> {

	public class Action extends Persistable {

		/** The handler that is currently active. */
		private String handlerName;
		/** The actionName of the action that is being executed on this configuration. */
		private String actionName;
		/** The actionName of the currently executing index. */
		private String indexName;
		/** The time the action was started. */
		private long startTime;

		public Action() {
		}

		public Action(String handlerName, String actionName, String indexName, long startTime) {
			this.handlerName = handlerName;
			this.actionName = actionName;
			this.indexName = indexName;
			this.startTime = startTime;
		}

		public String getHandlerName() {
			return handlerName;
		}

		public void setHandlerName(String handlerName) {
			this.handlerName = handlerName;
		}

		public String getActionName() {
			return actionName;
		}

		public void setActionName(String actionName) {
			this.actionName = actionName;
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
			builder.append(", ");
			builder.append(getActionName());
			builder.append(", ");
			builder.append(getHandlerName());
			builder.append("]");
			return builder.toString();
		}

	}

	/** The address of this machine. */
	private String address;
	/** Whether this server is working. */
	private boolean working;
	/** The details about the action that this server is executing. */
	private Action action;

	public Server() {
		this.action = new Action();
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

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
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
