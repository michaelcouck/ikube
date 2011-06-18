package ikube;

import ikube.action.IAction;
import ikube.cluster.IClusterManager;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;

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
		// File dotDirectory = new File(".");
		// FileUtilities.deleteFiles(dotDirectory, IConstants.DATABASE_FILE);
		// FileUtilities.deleteFiles(dotDirectory, IConstants.TRANSACTION_FILES);
	}

	private static final Logger LOGGER = Logger.getLogger(IndexEngine.class);
	private transient List<IAction<IndexContext, Boolean>> actions;

	public IndexEngine() {
		IListener listener = new IListener() {
			@Override
			public void handleNotification(final Event event) {
				IndexEngine.this.handleNotification(event);
			}
		};
		ListenerManager.addListener(listener);
		LOGGER.info("Index engine : " + this);
	}

	protected void handleNotification(final Event event) {
		if (!event.getType().equals(Event.TIMER)) {
			return;
		}

		// If this server is working on anything then return
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		Server server = clusterManager.getServer();
		if (server.getWorking()) {
			LOGGER.debug("This server working : " + server);
			return;
		}

		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : indexContexts.values()) {
			if (actions == null || actions.isEmpty()) {
				LOGGER.warn("No actions configured for index engine : " + indexContext.getIndexName());
				continue;
			}
			LOGGER.info("Start working on index : " + indexContext.getIndexName() + ", server : " + server.getAddress());
			int thread = Thread.currentThread().hashCode();
			for (IAction<IndexContext, Boolean> action : actions) {
				boolean success = Boolean.FALSE;
				try {
					// Sleep for a random period to avoid one server always being first
					long sleep = (long) (((Math.random() * 3d)) * 1000d);
					LOGGER.debug(Logging.getString("Sleeping for : ", sleep, " milliseconds"));
					Thread.sleep(sleep);
					LOGGER.debug(Logging.getString("Executing action : ", action, thread));
					success = action.execute(indexContext);
				} catch (Exception e) {
					LOGGER.error("Exception executing action : " + action, e);
				}
				LOGGER.debug(Logging.getString("Action succeeded : ", success, action, thread));
			}
			LOGGER.info(Logging.getString("Finish working : ", this, server.getAddress()));
		}
	}

	public void setActions(final List<IAction<IndexContext, Boolean>> actions) {
		this.actions = actions;
	}

}