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

    IAnalyticsService getAnalyticsService() {
        return getBean(IAnalyticsService.class);
    }

}
