package ikube.model;

import java.util.concurrent.Callable;

/**
 * This class is the base class for callables that are distributed over the grid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-02-2014
 */
public class Task implements Callable {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object call() throws Exception {
        return Boolean.TRUE;
    }
}