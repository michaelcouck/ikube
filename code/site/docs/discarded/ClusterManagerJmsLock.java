package ikube.cluster.jms;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This class is the lock class that is passed around the cluster containing the unique address of the server in the cluster and the shout
 * time, which determines who gets the lock.
 * 
 * @author Michael Couck
 * @since 11.11.11
 * @version 01.00
 */
public class ClusterManagerJmsLock implements Serializable {

	/** The timestamp for the server that shouted first. */
	private long shout;
	/** The unique address of the server that sent the lock request. */
	private String address;
	/** Whether the lock was requested or granted. */
	private boolean locked;

	public ClusterManagerJmsLock(String address, long shout, boolean locked) {
		this.address = address;
		this.shout = shout;
		this.locked = locked;
	}

	public long getShout() {
		return shout;
	}

	public void setShout(long shout) {
		this.shout = shout;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}