package ikube.action.rule;

import ikube.IConstants;
import ikube.action.IAction;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.model.IndexContext;
import ikube.model.Rule;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Logging;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;

/**
 * @see IRuleInterceptor
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleInterceptor implements IRuleInterceptor {

	private static final transient Logger	LOGGER			= Logger.getLogger(RuleInterceptor.class);

	private final ScheduledExecutorService	executorService	= Executors.newScheduledThreadPool(10);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object decide(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object target = proceedingJoinPoint.getTarget();
		String actionName = target.getClass().getSimpleName();
		IndexContext<?> indexContext = null;
		boolean proceed = Boolean.FALSE;
		JEP jep = new JEP();
		// During the execution of the rules the cluster needs to be locked
		// completely for the duration of the evaluation of the rules because there
		// exists a race condition where the rules evaluate to true for server one, and evaluate
		// to true for server two before server one can set the values that would make server
		// two evaluate to false, so they both start the action they shouldn't start. Generally this
		// will never happen because the timers will be different, but in a very small percentage
		// of cases they overlap
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		boolean gotLock = clusterManager.lock(IConstants.IKUBE);
		try {
			Action modelAction = new Action();
			modelAction.setRules(new ArrayList<Rule>());
			if (!IAction.class.isAssignableFrom(target.getClass())) {
				LOGGER.warn("Can't intercept non action class, proceeding : " + target);
				return proceedingJoinPoint.proceed();
			} else if (!gotLock) {
				LOGGER.info("Couldn't aquire lock : ");
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
						Object result = null;
						for (IRule<IndexContext<?>> rule : classRules) {
							boolean evaluation = rule.evaluate(indexContext);
							String ruleName = rule.getClass().getSimpleName();
							Rule modelRule = new Rule();
							modelRule.setAction(modelAction);
							modelRule.setName(ruleName);
							modelRule.setResult(evaluation);
							modelAction.getRules().add(modelRule);
							jep.addVariable(ruleName, evaluation);
							if (LOGGER.isDebugEnabled()) {
								LOGGER.error(Logging.getString("Rule : ", rule, ", parameter : ", ruleName, ", evaluation : ", evaluation));
							}
						}
						String predicate = action.getRuleExpression();
						modelAction.setRuleExpression(predicate);
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
						modelAction.setResult(finalResult);
						if (finalResult) {
							// TODO Take a snapshot of the cluster at the time of the rule becoming
							// true and an index started, including the state of the indexes on the file
							// system and the other servers
							proceed = Boolean.TRUE;
						}
					}
				}
			}
			String indexName = indexContext != null ? indexContext.getIndexName() : null;
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(Logging.getString("Rule intercepter proceeding : ", proceed, target, actionName, indexName));
				printSymbolTable(jep, indexName);
			}
			modelAction.setActionName(actionName);
			modelAction.setIndexName(indexName);
			modelAction.setStartTime(new Timestamp(System.currentTimeMillis()));
			IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
			dataBase.persist(modelAction);
			if (proceed) {
				proceed(proceedingJoinPoint, actionName, indexName, modelAction);
			} else {
				modelAction.setEndTime(new Timestamp(System.currentTimeMillis()));
				modelAction.setDuration(modelAction.getEndTime().getTime() - modelAction.getStartTime().getTime());
				modelAction.setWorking(Boolean.FALSE);
				dataBase.merge(modelAction);
			}
		} catch (Throwable t) {
			LOGGER.error("Exception evaluating the rules : ", t);
		} finally {
			boolean unlocked = clusterManager.unlock(IConstants.IKUBE);
			LOGGER.debug("Unlocked : " + unlocked);
		}
		return proceed;
	}

	protected synchronized void proceed(final ProceedingJoinPoint proceedingJoinPoint, final String actionName, final String indexName,
			final Action modelAction) {
		try {
			long delay = 1;
			final IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
			Server server = clusterManager.getServer();
			modelAction.setServerName(server.getAddress());
			// We set the working flag in the action within the cluster lock when setting to true
			clusterManager.startWorking(actionName, indexName, "");
			executorService.schedule(new Runnable() {
				public void run() {
					IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
					try {
						modelAction.setWorking(Boolean.TRUE);
						dataBase.merge(modelAction);
						proceedingJoinPoint.proceed();
					} catch (Throwable e) {
						LOGGER.error("Exception proceeding on join point : " + proceedingJoinPoint, e);
					} finally {
						modelAction.setEndTime(new Timestamp(System.currentTimeMillis()));
						modelAction.setDuration(modelAction.getEndTime().getTime() - modelAction.getStartTime().getTime());
						modelAction.setWorking(Boolean.FALSE);
						dataBase.merge(modelAction);
					}
				}
			}, delay, TimeUnit.MILLISECONDS);
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