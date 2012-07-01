package ikube.action.rule;

import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;

/**
 * This rule is to limit the number of actions an instance can perform concurrently. Essentially there can be 100 indexes defined and the
 * same server can start doing all the indexing and the performance will drop because of time switching and so on.
 * 
 * @author Michael Couck
 * @since 14.01.2012
 * @version 01.00
 */
public class TooManyActionsRule extends ARule<IndexContext<?>> {

	@Value("${max.actions}")
	private int maxActions;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		final Server server = clusterManager.getServer();
		final List<Action> actions = server.getActions();
		if (actions != null && actions.size() >= maxActions) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

}
