package ikube.action.rule;

import ikube.model.IndexContext;

import java.io.Serializable;
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
        return new IsIndexCurrent().evaluate(indexContext);
    }

}
