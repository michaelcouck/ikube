package ikube.action.rule;

import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Server;

import java.util.List;

/**
 * This rule checks if this server is working this index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03.02.2014
 */
public class IsThisServerWorkingThisIndex extends ARule<IndexContext<?>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(final IndexContext<?> indexContext) {
        Server server = clusterManager.getServer();
        List<Action> actions = server.getActions();
        if (actions != null) {
            for (final Action action : actions) {
                if (indexContext.getIndexName().equals(action.getIndexName())) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

}
