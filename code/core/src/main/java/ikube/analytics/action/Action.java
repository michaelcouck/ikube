package ikube.analytics.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * This is a base class for analytics actions that can be distributed in the cluster.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public abstract class Action<T> implements Callable<T> {

    Logger logger = LoggerFactory.getLogger(this.getClass());

}
