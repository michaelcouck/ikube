package ikube.action.rule;

import ikube.IConstants;
import ikube.action.IAction;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see IRuleInterceptor
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleInterceptor implements IRuleInterceptor {

	private static final transient Logger LOGGER = Logger.getLogger(RuleInterceptor.class);

	@Autowired
	private IClusterManager clusterManager;
	private ScheduledExecutorService executorService;

	public RuleInterceptor() {
		executorService = Executors.newScheduledThreadPool(10);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object decide(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object target = proceedingJoinPoint.getTarget();
		boolean proceed = Boolean.FALSE;
		// During the execution of the rules the cluster needs to be locked
		// completely for the duration of the evaluation of the rules because there
		// exists a race condition where the rules evaluate to true for server one, and evaluate
		// to true for server two before server one can set the values that would make server
		// two evaluate to false, so they both start the action they shouldn't start. Generally this
		// will never happen because the timers will be different, but in a very small percentage
		// of cases they overlap
		IndexContext<?> indexContext = null;
		try {
			String indexName = null;
			String actionName = target.getClass().getSimpleName();
			boolean gotLock = clusterManager.lock(IConstants.IKUBE);
			JEP jep = new JEP();
			if (!IAction.class.isAssignableFrom(target.getClass())) {
				LOGGER.warn("Can't intercept non action class, proceeding : " + target);
				proceed = Boolean.TRUE;
			} else if (clusterManager.getServer().getWorking()) {
				LOGGER.debug("Server already working : ");
				proceed = Boolean.FALSE;
			} else if (!gotLock) {
				LOGGER.debug("Couldn't aquire lock : ");
				proceed = Boolean.FALSE;
			} else {
				// Get the rules associated with this action
				@SuppressWarnings("rawtypes")
				IAction action = (IAction) target;
				@SuppressWarnings({ "unchecked" })
				List<IRule<IndexContext<?>>> classRules = action.getRules();
				if (classRules == null || classRules.size() == 0) {
					LOGGER.info("No rules defined, proceeding : " + target);
					proceed = Boolean.TRUE;
				} else {
					// Find the index context
					Object[] args = proceedingJoinPoint.getArgs();
					for (Object arg : args) {
						if (arg != null) {
							if (IndexContext.class.isAssignableFrom(arg.getClass())) {
								indexContext = (IndexContext<?>) arg;
							}
						}
					}
					if (indexContext == null) {
						LOGGER.warn("Couldn't find the index context : " + proceedingJoinPoint);
					} else {
						indexName = indexContext.getIndexName();
						Object result = null;
						for (IRule<IndexContext<?>> rule : classRules) {
							boolean evaluation = rule.evaluate(indexContext);
							String ruleName = rule.getClass().getSimpleName();
							jep.addVariable(ruleName, evaluation);
							if (LOGGER.isDebugEnabled()) {
								LOGGER.error(Logging.getString("Rule : ", rule, ", parameter : ", ruleName, ", evaluation : ", evaluation));
							}
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
						boolean finalResult = result != null && (result.equals(1.0d) || result.equals(Boolean.TRUE));
						if (finalResult) {
							// TODO Take a snapshot of the cluster at the time of the rule becoming
							// true and an index started, including the state of the indexes on the file
							// system and the other servers
							proceed = Boolean.TRUE;
						}
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug(Logging.getString("Rule intercepter proceeding : ", proceed, target, actionName, indexName));
							printSymbolTable(jep, indexName);
						}
					}
				}
			}
			if (proceed) {
				proceed(proceedingJoinPoint, actionName, indexName);
			}
		} catch (Exception t) {
			LOGGER.error("Exception evaluating the rules : target : " + target + ", context : " + indexContext, t);
		} finally {
			boolean unlocked = clusterManager.unlock(IConstants.IKUBE);
			LOGGER.debug("Unlocked : " + unlocked);
		}
		return proceed;
	}

	protected synchronized void proceed(final ProceedingJoinPoint proceedingJoinPoint, final String actionName, final String indexName) {
		long delay = 1;
		try {
			// We set the working flag in the action within the cluster lock when setting to true
			// clusterManager.startWorking(actionName, indexName, "");
			ScheduledFuture<?> scheduledFuture = executorService.schedule(new Runnable() {
				public void run() {
					try {
						proceedingJoinPoint.proceed();
					} catch (Throwable e) {
						LOGGER.error("Exception proceeding on join point : " + proceedingJoinPoint, e);
					}
					// finally {
					// clusterManager.stopWorking(actionName, indexName, "");
					// }
				}
			}, delay, TimeUnit.MILLISECONDS);
			long maxWait = 3000;
			long start = System.currentTimeMillis();
			while (!scheduledFuture.isDone()) {
				ThreadUtilities.sleep(100);
				long waitedFor = System.currentTimeMillis() - start;
				LOGGER.info("Waited : " + waitedFor);
				if (waitedFor > maxWait) {
					break;
				}
			}
		} finally {
			notifyAll();
		}
	}

	protected void printSymbolTable(final JEP jep, final String indexName) {
		try {
			SymbolTable symbolTable = jep.getSymbolTable();
			LOGGER.info("Index : " + indexName);
			LOGGER.info("Symbol table : " + symbolTable);
		} catch (Exception e) {
			LOGGER.error("Exception printing the nodes : ", e);
		}
	}

}