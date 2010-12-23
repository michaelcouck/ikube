package ikube;

import ikube.action.IAction;
import ikube.cluster.IClusterManager;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.logging.Logging;
import ikube.model.Event;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class is the central class for creating indexes.
 * 
 * This class then only looks up the index contexts and executes actions on them. The index engine registers a listener with the scheduler
 * and responds to the {@link Event#TIMER} type of event. This event schedule can be configured in the configuration, as can most schedules
 * and executors.
 * 
 * Index contexts contain parameters and indexables. Indexables are objects that can be indexed, like files and databases.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexEngine implements IIndexEngine {

	static {
		// Try to clean the database files
		// TODO - could be dangerous, perhaps to remove please?
		File dotDirectory = new File(".");
		FileUtilities.deleteFiles(dotDirectory, IConstants.DATABASE_FILE);
		FileUtilities.deleteFiles(dotDirectory, IConstants.TRANSACTION_FILES);
	}

	private Logger logger;
	private List<IAction<IndexContext, Boolean>> actions;

	public IndexEngine() {
		logger = Logger.getLogger(this.getClass());
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

		// If this server is working on anything then return
		if (ApplicationContextManager.getBean(IClusterManager.class).getServer().isWorking()) {
			logger.debug("This server working : " + ApplicationContextManager.getBean(IClusterManager.class).getServer());
			return;
		}

		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : indexContexts.values()) {
			if (actions == null || actions.size() == 0) {
				logger.warn("No actions configured for index engine : " + indexContext.getIndexName());
				continue;
			}
			logger.info("Starting working : " + indexContext);
			int thread = Thread.currentThread().hashCode();
			for (IAction<IndexContext, Boolean> action : actions) {
				logger.debug(Logging.getString("Executing action : ", action, ", ", thread));
				boolean success = Boolean.FALSE;
				try {
					// Sleep for a random period to avoid one server always being first
					long sleep = (long) (((Math.random() * 10d)) * 1000d);
					logger.info("Sleeping for : " + sleep + " milliseconds");
					Thread.sleep(sleep);
					success = action.execute(indexContext);
				} catch (Exception e) {
					logger.error("Exception executing action : " + action, e);
				}
				logger.debug(Logging.getString("Action succeeded : ", success, ", ", action, ", ", thread));
			}
			logger.info(Logging.getString("Finished working : ", this, ", ", ApplicationContextManager.getBean(IClusterManager.class)
					.getServer()));
		}
	}

	public void setActions(List<IAction<IndexContext, Boolean>> actions) {
		this.actions = actions;
	}

}