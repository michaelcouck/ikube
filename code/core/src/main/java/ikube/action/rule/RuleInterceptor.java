package ikube.action.rule;

import ikube.IConstants;
import ikube.action.IAction;
import ikube.action.Index;
import ikube.action.Open;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.toolkit.ThreadUtilities;

import java.util.List;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is implemented as an intercepter, and typically configured in Spring. The intercepter will intercept the execution of the actions, like
 * {@link Index} and {@link Open}. Each action has associated with it rules, like whether any other servers are currently working on this index or if the index
 * is current and already open. The rules for the action will then be executed, and based on the category of the boolean predicate parameterized with the
 * results of each rule, the action will either be executed or not. {@link JEP} is the expression parser for the rules.
 * 
 * @see IRuleInterceptor
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleInterceptor implements IRuleInterceptor {

	private static final Logger LOGGER = Logger.getLogger(RuleInterceptor.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public synchronized Object decide(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		try {
			Object target = proceedingJoinPoint.getTarget();
			boolean proceed = Boolean.TRUE;
			IndexContext<?> indexContext = null;
			if (!IAction.class.isAssignableFrom(target.getClass())) {
				LOGGER.warn("Can't intercept non action class, proceeding : " + target);
			} else {
				IAction action = (IAction) target;
				try {
					boolean proceedWithLocked = Boolean.TRUE;
					if (action.requiresClusterLock()) {
						proceedWithLocked = clusterManager.lock(IConstants.IKUBE);
					}
					if (!proceedWithLocked) {
						LOGGER.info("Couldn't get cluster lock : " + proceedingJoinPoint.getTarget());
						proceed = Boolean.FALSE;
					} else {
						// Find the index context
						indexContext = getIndexContext(proceedingJoinPoint);
						proceed = evaluateRules(indexContext, action);
					}
				} catch (NullPointerException e) {
					LOGGER.warn("Context closing down : ");
				} catch (Exception t) {
					LOGGER.error("Exception proceeding on target : " + target, t);
				} finally {
					clusterManager.unlock(IConstants.IKUBE);
				}
			}
			if (proceed) {
				proceed(indexContext, proceedingJoinPoint);
			}
		} finally {
			this.notifyAll();
		}
		return Boolean.TRUE;
	}

	/**
	 * Proceeds on the join point. A scheduled task will be started by the scheduler. The task is the action that has been given the green light to start. The
	 * current thread will wait for the action to complete, but will only wait for a few seconds then continue. The action is started in a separate thread
	 * because we don't want a queue of actions building up.
	 * 
	 * @param proceedingJoinPoint the intercepted action join point
	 */
	protected synchronized void proceed(final IndexContext<?> indexContext, final ProceedingJoinPoint proceedingJoinPoint) {
		try {
			// We set the working flag in the action within the cluster lock when setting to true
			Runnable runnable = new Runnable() {
				public void run() {
					Action action = null;
					try {
						// Start the action in the cluster
						String actionName = proceedingJoinPoint.getTarget().getClass().getSimpleName();
						action = clusterManager.startWorking(actionName, indexContext.getIndexName(), null);
						// Execute the action logic
						proceedingJoinPoint.proceed();
					} catch (Throwable e) {
						LOGGER.error("Exception proceeding on join point : " + proceedingJoinPoint, e);
					} finally {
						// Remove the action in the cluster
						clusterManager.stopWorking(action);
						ThreadUtilities.destroy(this.toString());
					}
				}
			};
			ThreadUtilities.submit(runnable.toString(), runnable);
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method will take the rules for the action and execute them, returning the category from the boolean rule predicate.
	 * 
	 * @param indexContext the index context for the index
	 * @param action the action who's rules are to be executed
	 * @return the category from the execution of the rules for the action
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean evaluateRules(final IndexContext<?> indexContext, final IAction action) {
		boolean finalResult = Boolean.TRUE;
		// Get the rules associated with this action
		List<IRule<IndexContext<?>>> rules = action.getRules();
		if (rules == null || rules.size() == 0) {
			LOGGER.info("No rules defined, proceeding : " + action);
		} else {
			JexlEngine jexlEngine = new JexlEngine();
			JexlContext jexlContext = new MapContext();
			Object result = null;
			for (IRule<IndexContext<?>> rule : rules) {
				boolean evaluation = rule.evaluate(indexContext);
				String ruleName = rule.getClass().getSimpleName();
				jexlContext.set(ruleName, evaluation);
			}
			String predicate = action.getRuleExpression();
			Expression expression = jexlEngine.createExpression(predicate);
			result = expression.evaluate(jexlContext);
			finalResult = result != null && (result.equals(1.0d) || result.equals(Boolean.TRUE));
		}
		return finalResult;
	}

	/**
	 * This method iterates through the join point arguments and looks for the index context.
	 * 
	 * @param proceedingJoinPoint the intercepted action join point
	 * @return the index context from the arguments or null if it can not be found
	 */
	protected IndexContext<?> getIndexContext(final ProceedingJoinPoint proceedingJoinPoint) {
		Object[] args = proceedingJoinPoint.getArgs();
		for (Object arg : args) {
			if (arg != null) {
				if (IndexContext.class.isAssignableFrom(arg.getClass())) {
					return (IndexContext<?>) arg;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	void log(final IndexContext indexContext, final Action action) {
		if (indexContext.getName().equals(IConstants.GEOSPATIAL)) {
			LOGGER.info("Evaluating : " + indexContext.getName() + ", " + action.getClass().getSimpleName());
		}
		// if (indexContext.getName().equals(IConstants.GEOSPATIAL)) {
		// LOGGER.info("    : " + ruleName + "-" + evaluation);
		// }
		// if (indexContext.getName().equals(IConstants.GEOSPATIAL)) {
		// LOGGER.info("    : " + finalResult);
		// LOGGER.info("    : " + action.getRuleExpression());
		// }
	}

}