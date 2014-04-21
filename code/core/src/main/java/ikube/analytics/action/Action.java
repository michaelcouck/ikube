package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;

import java.util.concurrent.Callable;

import static ikube.toolkit.ApplicationContextManager.getBean;

/**
 * This is a base class for analytics actions that can be distributed in the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public abstract class Action<T> implements Callable<T> {

    /**
     * This method returns the analytics service from Spring because we are now on the
     * remote machine, and the grid will not inject anything into our classes, we have to go
     * and get the logic that we need from the context.
     *
     * @return the analytics service from Spring, this is on the remote machine
     */
    public IAnalyticsService getAnalyticsService() {
        return getBean(IAnalyticsService.class);
    }

}
