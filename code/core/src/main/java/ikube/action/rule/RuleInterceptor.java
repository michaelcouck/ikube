package ikube.action.rule;

import ikube.IConstants;
import ikube.action.IAction;
import ikube.action.Index;
import ikube.action.Open;
import ikube.cluster.IClusterManager;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.toolkit.ThreadUtilities;

import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is implemented as an intercepter, and typically configured in Spring. The intercepter will intercept the execution of the
 * actions, like {@link Index} and {@link Open}. Each action has associated with it rules, like whether any other servers are currently
 * working on this index or if the index is current and already open. The rules for the action will then be executed, and based on the
 * result of the boolean predicate parameterized with the results of each rule, the action will either be executed or not. {@link JEP} is
 * the expression parser for the rules.
 * 
 * @see IRuleInterceptor
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleInterceptor implements IRuleInterceptor {

	private static final Logger LOGGER = Logger.getLogger(RuleInterceptor.class);

	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object decide(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object target = proceedingJoinPoint.getTarget();
		boolean proceed = Boolean.FALSE;
		IndexContext<?> indexContext = null;
		try {
			if (!IAction.class.isAssignableFrom(target.getClass())) {
				LOGGER.warn("Can't intercept non action class, proceeding : " + target);
				proceed = Boolean.TRUE;
			} else if (!clusterManager.lock(IConstants.IKUBE)) {
				LOGGER.info("Couldn't get cluster lock : " + proceedingJoinPoint.getTarget());
				proceed = Boolean.FALSE;
			} else {
				// Find the index context
				indexContext = getIndexContext(proceedingJoinPoint);
				if (indexContext == null) {
					LOGGER.warn("Couldn't find the index context : " + proceedingJoinPoint);
				} else {
					IAction action = (IAction) target;
					proceed = evaluateRules(indexContext, action);
				}
			}
			if (proceed) {
				proceed(indexContext, proceedingJoinPoint);
			}
		} catch (NullPointerException e) {
			LOGGER.warn("Context closing down : ");
		} catch (Exception t) {
			LOGGER.error("Exception proceeding on target : " + target, t);
		} finally {
			boolean unlocked = clusterManager.unlock(IConstants.IKUBE);
			LOGGER.debug("Unlocked : " + unlocked);
		}
		return Boolean.TRUE;
	}

	/**
	 * Proceeds on the join point. A scheduled task will be started by the scheduler. The task is the action that has been given the green
	 * light to start. The current thread will wait for the action to complete, but will only wait for a few seconds then continue. The
	 * action is started in a separate thread because we don't want a queue of actions building up.
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
					}
				}
			};
			ThreadUtilities.submit(null, runnable);
		} finally {
			notifyAll();
		}
	}

	/**
	 * This method will take the rules for the action and execute them, returning the result from the boolean rule predicate.
	 * 
	 * @param indexContext the index context for the index
	 * @param action the action who's rules are to be executed
	 * @return the result from the execution of the rules for the action
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean evaluateRules(final IndexContext<?> indexContext, final IAction action) {
		boolean finalResult = Boolean.TRUE;
		// Get the rules associated with this action
		List<IRule<IndexContext<?>>> rules = action.getRules();
		if (rules == null || rules.size() == 0) {
			LOGGER.info("No rules defined, proceeding : " + action);
		} else {
			JEP jep = new JEP();
			Object result = null;
			for (IRule<IndexContext<?>> rule : rules) {
				boolean evaluation = rule.evaluate(indexContext);
				String ruleName = rule.getClass().getSimpleName();
				jep.addVariable(ruleName, evaluation);
			}
			String predicate = action.getRuleExpression();
			jep.parseExpression(predicate);
			if (jep.hasError()) {
				LOGGER.warn("Exception in Jep expression : " + jep.getErrorInfo());
				LOGGER.warn("Symbol table : " + jep.getSymbolTable());
			}
			result = jep.getValueAsObject();
			if (result == null) {
				result = jep.getValue();
			}
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

	protected void printSymbolTable(final JEP jep, final String indexName, final String target) {
		try {
			SymbolTable symbolTable = jep.getSymbolTable();
			LOGGER.info("Symbol table : " + indexName + ", " + target + " : " + symbolTable);
		} catch (Exception e) {
			LOGGER.error("Exception printing the nodes : ", e);
		}
	}

}