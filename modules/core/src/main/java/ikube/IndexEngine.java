package ikube;

import ikube.action.IAction;
import ikube.cluster.IClusterManager;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexEngine implements IIndexEngine {

	static {
		// Try to clean the database files
		File dotDirectory = new File(".");
		FileUtilities.deleteFiles(dotDirectory, IConstants.DATABASE_FILE);
		FileUtilities.deleteFiles(dotDirectory, IConstants.TRANSACTION_FILES);
	}

	private Logger logger;
	private List<IAction<IndexContext, Boolean>> actions;

	public IndexEngine() {
		logger = Logger.getLogger(this.getClass());

		ListenerManager.addListener(new IListener() {
			@Override
			public void handleNotification(Event event) {
				// We set the cluster object if not set already
				IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
				Server server = clusterManager.getServer();
				clusterManager.set(Server.class, server.getId(), server);

				Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
				logger.debug("Contexts : " + indexContexts);
				for (IndexContext indexContext : indexContexts.values()) {
					long idNumber = clusterManager.getIdNumber(indexContext.getIndexName(), indexContext.getBatchSize());
					logger.debug("Id number : " + idNumber);
				}
				ListenerManager.removeListener(this);
			}
		});

		IListener listener = new IListener() {
			@Override
			public void handleNotification(Event event) {
				IndexEngine.this.handleNotification(event);
			}
		};
		ListenerManager.addListener(listener);
		logger.info("Index engine : " + this);
	}

	protected void handleNotification(Event event) {
		if (!event.getType().equals(Event.TIMER)) {
			return;
		}
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		Server server = clusterManager.getServer();

		// If this server is working on anything then return
		if (server.isWorking()) {
			logger.debug("This server working : " + server);
			return;
		}

		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		logger.debug("Contexts : " + indexContexts);
		for (IndexContext indexContext : indexContexts.values()) {
			if (actions == null || actions.size() == 0) {
				logger.warn("No actions configured for index engine : " + indexContext.getIndexName());
				continue;
			}
			try {
				logger.info("Starting working : " + indexContext);
				for (IAction<IndexContext, Boolean> action : actions) {
					logger.debug("Executing action : " + action + ", " + Thread.currentThread().hashCode());
					boolean success = Boolean.FALSE;
					try {
						success = action.execute(indexContext);
					} catch (Exception e) {
						logger.error("Exception executing action : " + action, e);
					}
					logger.debug("Action succeeded : " + success + ", " + Thread.currentThread().hashCode());
				}
				logger.info("Finished working : " + this);
			} catch (Exception e) {
				logger.error("Exception in the index engine : " + indexContext.getIndexName() + ", " + indexContext.getName(), e);
			} finally {
				clusterManager.setWorking(indexContext.getIndexName(), null, null, Boolean.FALSE);
			}
		}

	}

	public void setActions(List<IAction<IndexContext, Boolean>> actions) {
		this.actions = actions;
	}

}
