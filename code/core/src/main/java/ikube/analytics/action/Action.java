package ikube.analytics.action;

import ikube.analytics.IAnalyticsService;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static ikube.toolkit.ApplicationContextManager.getBean;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public abstract class Action<T> implements Callable<T>, Serializable {

    IAnalyticsService getAnalyticsService() {
        return getBean(IAnalyticsService.class);
    }

}
