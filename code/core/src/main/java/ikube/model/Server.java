package ikube.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

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

	@Transient
	private String logTail;
	@Transient
	private boolean show;
	@Transient
	private boolean cpuThrottling = Boolean.TRUE;
	@Transient
	private boolean threadsRunning = Boolean.TRUE;

	/** The age of this server. */
	@Column
	private long age;
	/** The ip of the server. */
	@Column
	private String ip;
	/** The address of this machine. */
	@Column
	private String address;
	/** The actions of this server. */
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "server", fetch = FetchType.LAZY)
	private List<Action> actions;
	@SuppressWarnings("rawtypes")
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private List<IndexContext> indexContexts;

	@Column
	private long freeMemory;
	@Column
	private long maxMemory;
	@Column
	private long totalMemory;
	@Column
	private long freeDiskSpace;
	@Column
	private String architecture;
	@Column
	private long processors;
	@Column
	private double averageCpuLoad;

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
		if (actions == null) {
			actions = new ArrayList<Action>();
		}
		return actions;
	}

	public void setActions(final List<Action> actions) {
		this.actions = actions;
	}

	@SuppressWarnings("rawtypes")
	public List<IndexContext> getIndexContexts() {
		return indexContexts;
	}

	@SuppressWarnings("rawtypes")
	public void setIndexContexts(final List<IndexContext> indexContexts) {
		this.indexContexts = indexContexts;
	}

	public boolean isWorking() {
		if (getActions() != null) {
			for (Action action : getActions()) {
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

	public long getAge() {
		return age;
	}

	public void setAge(final long age) {
		this.age = age;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(final long freeMemory) {
		this.freeMemory = freeMemory;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public void setMaxMemory(final long maxMemory) {
		this.maxMemory = maxMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(final long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public long getFreeDiskSpace() {
		return freeDiskSpace;
	}

	public void setFreeDiskSpace(final long freeDiskSpace) {
		this.freeDiskSpace = freeDiskSpace;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(final String architecture) {
		this.architecture = architecture;
	}

	public long getProcessors() {
		return processors;
	}

	public void setProcessors(final long processors) {
		this.processors = processors;
	}

	public double getAverageCpuLoad() {
		return averageCpuLoad;
	}

	public void setAverageCpuLoad(final double averageCpuLoad) {
		this.averageCpuLoad = averageCpuLoad;
	}

	public String getLogTail() {
		return logTail;
	}

	public void setLogTail(String logTail) {
		this.logTail = logTail;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public boolean isCpuThrottling() {
		return cpuThrottling;
	}

	public void setCpuThrottling(boolean cpuThrottling) {
		this.cpuThrottling = cpuThrottling;
	}

	public boolean isThreadsRunning() {
		return threadsRunning;
	}

	public void setThreadsRunning(boolean threadsRunning) {
		this.threadsRunning = threadsRunning;
	}

	public boolean equals(final Object object) {
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
		return (int) this.getId();
	}

	@Override
	public int compareTo(final Server other) {
		return this.getAddress().compareTo(other.getAddress());
	}

}