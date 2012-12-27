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
public class Event implements Serializable {

	public static final String TIMER = "timer";
	public static final String TIMER_DELTA = "timer-delta";
	public static final String PERFORMANCE = "performance";
	public static final String SERVER_RELEASE = "serverRelease";

	public static final String STARTUP = "startup";
	public static final String STARTUP_ALL = "startup-all";
	public static final String TERMINATE = "terminate";
	public static final String TERMINATE_ALL = "terminate-all";
	
	public static final String DELETE_INDEX = "delete-index";

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
