package ikube.listener;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Events are fired by clients and received by interested parties who can act on them, for example the end of an indexing process might
 * trigger the opening of that index.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Event {

	public static final String TIMER = "timer";
	public static final String PERFORMANCE = "performance";
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
	public static final String ALIVE = "alive";
	public static final String CLEAN = "clean";
	public static final String JMS = "jms";
	public static final String LOCK_RELEASE = "lockRelease";
	public static final String SERVER_RELEASE = "serverRelease";
	public static final String SERVER_CLUB = "serverClub";
	public static final String STARTUP = "startup";
	public static final String TERMINATE = "terminate";

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

	public void setObject(final Serializable object) {
		this.object = object;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
