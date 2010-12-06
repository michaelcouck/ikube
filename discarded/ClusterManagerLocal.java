package ikube.cluster;

import ikube.model.IndexContext;
import ikube.model.Server;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.jgroups.Address;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterManagerLocal implements IClusterManager {

	private Server server = new Server();
	{
		Address address = null;
		server.setAddress(address);
		server.setIp("127.0.0.1");
	}

	@Override
	public synchronized boolean anyWorking(String actionName) {
		try {
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean areWorking(String indexName, String actionName) {
		try {
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized long getLastWorkingTime(String indexName, String actionName) {
		long time = System.currentTimeMillis();
		try {
		} finally {
			notifyAll();
		}
		return time;
	}

	@Override
	public synchronized long getIdNumber(String indexName) {
		long idNumber = 0;
		try {
			return idNumber;
		} finally {
			notifyAll();
		}
	}

	public synchronized void setIdNumber(String indexName, long idNumber) {
		try {
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Set<Server> getServers() {
		try {
			return new TreeSet<Server>(Arrays.asList(server));
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Server getServer() {
		try {
			return server;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized void setWorking(IndexContext indexContext, String actionName, boolean isWorking, long start) {
		try {
			indexContext.setAction(actionName);
			indexContext.setStart(start);
			indexContext.setWorking(isWorking);
			// We add the context here which will then over ride the context in the local server. This need't
			// be done every time as the local server object is always local and in this Jvm the contexts in the
			// server are the instances from the configuration, not from the other servers
		} finally {
			notifyAll();
		}
	}

	@Override
	public boolean anyWorkingOnIndex(IndexContext indexContext) {
		return Boolean.FALSE;
	}

}