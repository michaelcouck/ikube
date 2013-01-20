package ikube;

import ikube.action.IAction;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.model.IndexContext;
import ikube.service.IMonitorService;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

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

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private List<IAction<IndexContext<?>, Boolean>> actions;

	public IndexEngine() {
		LOGGER.info("Index engine : " + this);
		SerializationUtilities.setTransientFields(IndexContext.class, new ArrayList<Class<?>>());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void handleNotification(final Event event) {
		Random random = new Random();
		if (Event.TIMER.equals(event.getType())) {
			Collection<IndexContext> indexContexts = monitorService.getIndexContexts().values();
			for (IndexContext<?> indexContext : indexContexts) {
				if (indexContext.isDelta()) {
					continue;
				}
				processIndexContext(indexContext, random);
			}
		} else if (Event.TIMER_DELTA.equals(event.getType())) {
			Collection<IndexContext> indexContexts = monitorService.getIndexContexts().values();
			for (IndexContext<?> indexContext : indexContexts) {
				if (!indexContext.isDelta()) {
					continue;
				}
				processIndexContext(indexContext, random);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void processIndexContext(final IndexContext indexContext, final Random random) {
		for (final IAction<IndexContext<?>, Boolean> action : actions) {
			try {
				Runnable runnable = new Runnable() {
					public void run() {
						try {
							action.execute(indexContext);
						} catch (Throwable e) {
							LOGGER.error("Exception executing action : " + action, e);
						}
					}
				};
				Future<?> future = ThreadUtilities.submit(runnable);
				// We'll wait a few seconds for this action, perhaps it is a fast one
				ThreadUtilities.waitForFuture(future, 3);
			} catch (Exception e) {
				LOGGER.error("Exception executing action : " + action, e);
			}
		}
	}

	public void destroy() {
		actions.clear();
	}

}