package ikube;

import ikube.action.IAction;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.IndexContext;
import ikube.toolkit.ClusterManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;

public class IndexEngine implements IIndexEngine {

	private Logger logger = Logger.getLogger(this.getClass());
	private IndexContext indexContext;
	private List<IAction<IndexContext, Boolean>> actions;

	public IndexEngine(IndexContext indexContext) {
		this.indexContext = indexContext;
		this.getServerName();
		IListener listener = new IListener() {
			@Override
			public void handleNotification(Event event) {
				IndexEngine.this.handleNotification(event);
			}
		};
		ListenerManager.addListener(listener);
		logger.info("Index engine : " + this + ", index : " + indexContext.getIndexName() + ", server : " + indexContext.getServerName());
	}

	private void handleNotification(Event event) {
		if (actions == null) {
			logger.warn("No actions configured for index engine : " + indexContext.getIndexName());
		}
		// Execute the logic to start the indexing
		if (event.getType().equals(Event.TIMER)) {
			logger.debug("Notification : " + this + ", " + event);
			try {
				if (ClusterManager.isWorking(indexContext)) {
					logger.info("Server already working : " + indexContext.getIndexName() + ", " + indexContext.getServerName());
					return;
				}
				logger.info("Starting working : " + this);
				for (IAction<IndexContext, Boolean> action : actions) {
					try {
						logger.debug("Executing action : " + action + ", " + Thread.currentThread().hashCode());
						action.execute(indexContext);
					} catch (Exception e) {
						logger.error("Exception executing action : " + action, e);
					}
				}
				logger.info("Finished working : " + this);
			} catch (Exception e) {
				logger.error("Exception in the index engine : " + indexContext.getIndexName() + ", " + indexContext.getServerName(), e);
			}
		}
	}

	private String getServerName() {
		if (indexContext.getServerName() == null) {
			try {
				indexContext.setServerName(InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				indexContext.setServerName("localhost?");
				logger.error("Exception accessing the localhost?", e);
			}
		}
		return indexContext.getServerName();
	}

	public void setActions(List<IAction<IndexContext, Boolean>> actions) {
		this.actions = actions;
	}

}
