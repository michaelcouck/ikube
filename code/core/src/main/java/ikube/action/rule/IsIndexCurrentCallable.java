package ikube.action.rule;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This class is just a wrapper for the {@link ikube.action.rule.IsIndexCurrent} rule that can
 * be executed over the wire using the grid, on a target remote machine, ultimately the result will be
 * whether the remote server has a current index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 30-03-2014
 */
public class IsIndexCurrentCallable implements Callable<Boolean>, Serializable {

    private IndexContext indexContext;

    public IsIndexCurrentCallable(final IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean call() throws Exception {
		// Get the local index context, as we are now on the remote machine
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (final Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
			if (indexContext.getName().equals(mapEntry.getValue().getName())) {
				@SuppressWarnings("UnnecessaryLocalVariable")
				boolean indexCurrent = new IsIndexCurrent().evaluate(mapEntry.getValue());
				// System.out.println("Remote index current : " + indexCurrent + ", " + indexContext);
				return indexCurrent;
			}
		}
		// System.out.println("Remote index not current : " + indexContext);
        return Boolean.FALSE;
    }

}