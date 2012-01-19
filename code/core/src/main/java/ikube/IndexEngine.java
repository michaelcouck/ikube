package ikube;

import ikube.action.IAction;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
public class IndexEngine implements IIndexEngine, IListener {

	private static final Logger LOGGER = Logger.getLogger(IndexEngine.class);

	private List<IAction<IndexContext<?>, Boolean>> actions;

	public IndexEngine() {
		LOGGER.info("Index engine : " + this);
		SerializationUtilities.setTransientFields(IndexContext.class, new ArrayList<Class<?>>());
	}

	@Override
	public void handleNotification(final Event event) {
		if (!Event.TIMER.equals(event.getType())) {
			return;
		}
		Random random = new Random();
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext<?> indexContext : indexContexts.values()) {
			if (actions == null || actions.isEmpty()) {
				LOGGER.warn("No actions configured for index engine : " + indexContext.getIndexName());
				continue;
			}
			LOGGER.debug(Logging.getString("Start working : ", indexContext.getIndexName()));
			for (IAction<IndexContext<?>, Boolean> action : actions) {
				try {
					action.execute(indexContext);
					ThreadUtilities.sleep(Math.abs(random.nextLong()) % 3000l);
				}catch (InterruptedException e) {
					LOGGER.warn("Sleep interrupted : " + action + ", " + e.getMessage());
				} catch (Exception e) {
					LOGGER.error("Exception executing action : " + action, e);
				}
			}
			LOGGER.debug(Logging.getString("Finished working : ", indexContext.getIndexName()));
		}
	}

	public void setActions(final List<IAction<IndexContext<?>, Boolean>> actions) {
		this.actions = actions;
	}

}