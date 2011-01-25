package ikube.listener;

import java.io.Serializable;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Event {

	public static final String TIMER = "timer";
	public static final String PROFILE = "profile";
	public static final String SEARCHER_OPENED = "searcherOpened";
	public static final String RESULTS = "results";
	public static final String NO_RESULTS = "noResults";
	public static final String SERVICE = "service";
	public static final String DEAD_LOCK = "deadLock";
	public static final String REPORT = "report";
	public static final String CLUSTERING = "clustering";
	public static final String LINK = "link";
	public static final String SYNCHRONISE = "synchronise";
	public static final String VALIDATION = "validation";

	private String type;
	private long timestamp;
	private boolean consumed;
	/** And arbitrary object to pass around. */
	private Serializable object;

	public String getType() {
		return type;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public boolean isConsumed() {
		return consumed;
	}

	public void setConsumed(final boolean consumed) {
		this.consumed = consumed;
	}

	public Serializable getObject() {
		return object;
	}

	public void setObject(Serializable object) {
		this.object = object;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(getType()).append(", ");
		builder.append(getTimestamp()).append(", ");
		builder.append(isConsumed());
		builder.append("]");
		return builder.toString();
	}

}
