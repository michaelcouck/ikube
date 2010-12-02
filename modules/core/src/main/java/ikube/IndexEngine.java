package ikube;

import ikube.action.IAction;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexEngine implements IIndexEngine {

	private Logger logger = Logger.getLogger(this.getClass());
	private List<IAction<IndexContext, Boolean>> actions;

	public IndexEngine() {
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
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		logger.debug("Contexts : " + indexContexts);
		for (IndexContext indexContext : indexContexts.values()) {
			if (actions == null || actions.size() == 0) {
				logger.warn("No actions configured for index engine : " + indexContext.getIndexName());
				continue;
			}
			try {
				if (indexContext.isWorking()) {
					logger.info("Already working : " + indexContext.getIndexName() + ", " + indexContext.getName());
					continue;
				}
				indexContext.setWorking(Boolean.TRUE);
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
			}
		}

	}

	public void setActions(List<IAction<IndexContext, Boolean>> actions) {
		this.actions = actions;
	}

}
