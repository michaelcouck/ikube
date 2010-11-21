package ikube.cluster;

import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Token;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterManager implements IClusterManager {

	private Logger logger;
	private ILockManager lockManager;

	public ClusterManager() {
		this.logger = Logger.getLogger(this.getClass());
	}

	@Override
	public synchronized boolean anyWorking(String actionName) {
		try {
			Token token = lockManager.getToken();
			for (Server server : token.getServers()) {
				for (IndexContext indexContext : server.getIndexContexts()) {
					if (indexContext.isWorking() && !actionName.equals(indexContext.getAction())) {
						return Boolean.TRUE;
					}
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean areWorking(String indexName, String actionName) {
		try {
			Token token = lockManager.getToken();
			for (Server server : token.getServers()) {
				for (IndexContext indexContext : server.getIndexContexts()) {
					if (indexContext.isWorking() && indexName.equals(indexContext.getIndexName())
							&& actionName.equals(indexContext.getAction())) {
						return Boolean.TRUE;
					}
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized long getLastWorkingTime(String indexName, String actionName) {
		long time = System.currentTimeMillis();
		try {
			Token token = lockManager.getToken();
			for (Server server : token.getServers()) {
				for (IndexContext indexContext : server.getIndexContexts()) {
					if (!indexContext.isWorking()) {
						continue;
					}
					if (!indexName.equals(indexContext.getIndexName())) {
						continue;
					}
					// This means that there is a server working on this index but with a different action
					if (!actionName.equals(indexContext.getAction())) {
						logger.debug("Another action on this index : " + server);
						time = -1;
						break;
					}
					long start = indexContext.getStart();
					// We want the start time of the first server, so if the start time
					// of this server is lower than the existing start time then take that
					if (start < time && start > 0) {
						logger.info("Taking start time of server : " + token);
						time = start;
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug(Logging.getString("Time : ", time, ", ", token.getServers(), ", ", Thread.currentThread().hashCode()));
			}
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
	public synchronized long getIdNumber(String indexName) {
		long idNumber = 0;
		try {
			Token token = lockManager.getToken();
			for (Server server : token.getServers()) {
				for (IndexContext indexContext : server.getIndexContexts()) {
					if (indexContext.isWorking() && indexName.equals(indexContext.getIndexName())) {
						if (indexContext.getIdNumber() > idNumber) {
							idNumber = indexContext.getIdNumber();
						}
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug(Logging.getString("Get id number : ", idNumber, ", ", Thread.currentThread().hashCode()));
			}
			return idNumber;
		} finally {
			notifyAll();
		}
	}

	public synchronized void setIdNumber(String indexName, long idNumber) {
		try {
			Server server = lockManager.getServer();
			if (logger.isDebugEnabled()) {
				logger.debug(Logging.getString("Setting id number : ", idNumber, ", ", server, ", ", Thread.currentThread().hashCode()));
			}
			for (IndexContext indexContext : server.getIndexContexts()) {
				if (indexName.equals(indexContext.getIndexName())) {
					indexContext.setIdNumber(idNumber);
				}
			}
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Set<Server> getServers() {
		try {
			return lockManager.getToken().getServers();
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Server getServer() {
		try {
			return lockManager.getServer();
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
			lockManager.getServer().getIndexContexts().add(indexContext);
			if (logger.isDebugEnabled()) {
				logger.info(Logging.getString("Set working : ", indexContext, ", ", Thread.currentThread().hashCode()));
			}
		} finally {
			notifyAll();
		}
	}

	@Override
	public boolean anyWorkingOnIndex(IndexContext indexContext) {
		Iterator<Server> iterator = lockManager.getToken().getServers().iterator();
		while (iterator.hasNext()) {
			Server server = iterator.next();
			for (IndexContext otherIndexContext : server.getIndexContexts()) {
				if (indexContext.getName().equals(otherIndexContext.getName())) {
					continue;
				}
				if (!indexContext.getIndexName().equals(otherIndexContext.getIndexName())) {
					continue;
				}
				if (indexContext.isWorking()) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	public void setLockManager(ILockManager lockManager) {
		this.lockManager = lockManager;
	}

}