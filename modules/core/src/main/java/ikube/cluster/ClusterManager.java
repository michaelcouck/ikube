package ikube.cluster;

import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Token;

import java.util.Set;

import org.apache.log4j.Logger;

public class ClusterManager implements IClusterManager {

	private Logger logger;
	private ILockManager lockManager;

	public ClusterManager() {
		this.logger = Logger.getLogger(this.getClass());
	}

	@Override
	public synchronized boolean anyWorking(IndexContext indexContext, String actionName) {
		try {
			Token token = lockManager.getToken();
			Server thisServer = lockManager.getServer();
			for (Server server : token.getServers()) {
				if (thisServer.compareTo(server) == 0) {
					continue;
				}
				if (server.isWorking() && !actionName.equals(server.getAction())) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean areWorking(IndexContext indexContext, String actionName) {
		try {
			Token token = lockManager.getToken();
			Server thisServer = lockManager.getServer();
			for (Server server : token.getServers()) {
				if (thisServer.compareTo(server) == 0) {
					continue;
				}
				if (server.isWorking() && indexContext.getIndexName().equals(server.getIndex()) && actionName.equals(server.getAction())) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized long getLastWorkingTime(IndexContext indexContext, String actionName) {
		long time = System.currentTimeMillis();
		try {
			Token token = lockManager.getToken();
			Server thisServer = lockManager.getServer();
			logger.info("Servers : " + token.getServers() + ", " + Thread.currentThread().hashCode());
			for (Server server : token.getServers()) {
				if (!server.isWorking()) {
					continue;
				}
				if (!indexContext.getIndexName().equals(server.getIndex())) {
					continue;
				}
				// This means that there is a server working on this index but with a different action
				if (!actionName.equals(server.getAction())) {
					logger.debug("Another action on this index : " + server);
					time = -1;
					break;
				}
				long start = server.getStart();
				// We want the start time of the first server, so if the start time
				// of this server is lower than the existing start time then take that
				if (start < time && start > 0) {
					logger.info("Taking start time of server : " + token);
					time = start;
				}
			}
			thisServer.setStart(time);
			thisServer.setAction(actionName);
			thisServer.setIdNumber(0);
			thisServer.setWorking(Boolean.TRUE);
			logger.info("Time : " + time + ", " + token.getServers() + ", " + Thread.currentThread().hashCode());
		} finally {
			notifyAll();
		}
		return time;
	}

	/**
	 * TODO - this method is called from the visitors so we need to extract an action from this method so that the synchronisation aspect
	 * will wrap it in a 'transaction'
	 */
	@Override
	public synchronized long getIdNumber(IndexContext indexContext) {
		long idNumber = 0;
		try {
			Token token = lockManager.getToken();
			Server thisServer = lockManager.getServer();
			String index = indexContext.getIndexName();
			for (Server server : token.getServers()) {
				if (server.isWorking() && index.equals(server.getIndex())) {
					if (server.getIdNumber() > idNumber) {
						idNumber = server.getIdNumber();
					}
				}
			}
			logger.info("Get id number : " + idNumber + ", " + thisServer + ", " + Thread.currentThread().hashCode());
		} finally {
			notifyAll();
		}
		return idNumber;
	}

	public synchronized void setIdNumber(IndexContext indexContext, long idNumber) {
		try {
			Server thisServer = lockManager.getServer();
			logger.debug("Setting id number : " + idNumber + ", " + thisServer + ", " + Thread.currentThread().hashCode());
			thisServer.setIdNumber(idNumber);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Set<Server> getServers(IndexContext indexContext) {
		try {
			Token token = lockManager.getToken();
			return token.getServers();
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean isWorking(IndexContext indexContext) {
		try {
			Server thisServer = lockManager.getServer();
			return thisServer.isWorking();
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean resetWorkings(IndexContext indexContext, String actionName) {
		try {
			// Check that there are no servers working
			Token token = lockManager.getToken();
			for (Server server : token.getServers()) {
				if (server.isWorking()) {
					logger.info("Servers working, not resetting : " + Thread.currentThread().hashCode());
					return Boolean.FALSE;
				}
			}
			logger.info("No server working, resetting : " + token.getServers() + ", " + Thread.currentThread().hashCode());
			for (Server server : token.getServers()) {
				if (server.isWorking()) {
					server.setAction(null);
					server.setIdNumber(0);
					server.setStart(0);
					server.setWorking(Boolean.FALSE);
				}
			}
			return Boolean.TRUE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized void setWorking(IndexContext indexContext, String actionName, boolean isWorking, long start) {
		try {
			Server thisServer = lockManager.getServer();
			thisServer.setAction(actionName);
			thisServer.setIndex(indexContext.getIndexName());
			thisServer.setStart(start);
			thisServer.setWorking(isWorking);
			logger.debug("Set working : " + thisServer + ", " + Thread.currentThread().hashCode());
			// Thread.dumpStack();
		} finally {
			notifyAll();
		}
	}

}
