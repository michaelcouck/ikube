package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static ikube.toolkit.ApplicationContextManager.getBean;

/**
 * This is a base class for analytics actions that can be distributed in the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public abstract class Action<T> implements Callable<T>, Serializable {

    /**
     * Gets the service to execute the logic, specifically analytics logic like creating an
     * analyzer or doing an analysis. This calls the static {@link ikube.toolkit.ApplicationContextManager} class
     * for the service, because we are on the remote machine now of course, and dependency injection doesn't work
     * through the grid. Also this allows this class to be mocked out easier. Testability is a prime concern of
     * course.
     *
     * @return the analytics service on the machine where this snippit is running
     */
    IAnalyticsService getAnalyticsService() {
        return getBean(IAnalyticsService.class);
    }

}
