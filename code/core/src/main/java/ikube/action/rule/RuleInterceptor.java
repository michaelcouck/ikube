package ikube.action.rule;

import ikube.IConstants;
import ikube.action.IAction;
import ikube.cluster.AtomicAction;
import ikube.model.IndexContext;
import ikube.toolkit.Logging;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;

import com.hazelcast.core.ILock;

/**
 * @see IRuleInterceptor
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleInterceptor implements IRuleInterceptor {

	private static final transient Logger LOGGER = Logger.getLogger(RuleInterceptor.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object decide(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// TODO During the execution of the rules the cluster needs to be locked
		// completely for the duration of the evaluation of the rules because there
		// exists a race condition where the rules evaluate to true for server one, and evaluate
		// to true for server two before server one can set the values that would make server
		// two evaluate to false, so they both start the action they shouldn't start
		ILock lock = AtomicAction.lock(IConstants.SERVER_LOCK);
		JEP jep = new JEP();
		Object result = null;
		String predicate = null;
		boolean proceed = Boolean.FALSE;
		IndexContext indexContext = null;
		try {
			Object target = proceedingJoinPoint.getTarget();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Intercepting : " + target);
			}
			if (lock == null) {
				proceed = Boolean.FALSE;
			} else if (!IAction.class.isAssignableFrom(target.getClass())) {
				LOGGER.warn("Can't intercept non action class, proceeding : " + target);
				proceed = Boolean.TRUE;
			} else {
				// Get the rules associated with this action
				@SuppressWarnings({ "unchecked", "rawtypes" })
				List<IRule<IndexContext>> classRules = ((IAction) target).getRules();
				if (classRules == null) {
					LOGGER.warn("No rules defined for, proceeding : " + target);
					proceed = Boolean.TRUE;
				} else {
					Object[] args = proceedingJoinPoint.getArgs();
					for (Object arg : args) {
						if (arg != null && IndexContext.class.isAssignableFrom(arg.getClass())) {
							indexContext = (IndexContext) arg;
						}
					}
					if (indexContext != null) {
						for (IRule<IndexContext> rule : classRules) {
							boolean evaluation = rule.evaluate(indexContext);
							String parameter = rule.getClass().getSimpleName();
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug(Logging.getString("Rule : ", rule, ", parameter : ", parameter, ", evaluation : ", evaluation));
							}
							jep.addVariable(parameter, evaluation);
						}
						predicate = ((IAction<?, ?>) target).getRuleExpression();
						jep.parseExpression(predicate);
						if (jep.hasError()) {
							LOGGER.warn("Exception in Jep expression : " + jep.getErrorInfo());
							LOGGER.warn("Symbol table : " + jep.getSymbolTable());
						}
						result = jep.getValueAsObject();
						if (result == null) {
							result = jep.getValue();
						}
						if (LOGGER.isInfoEnabled()) {
							// Node node = jep.getTopNode();
							// printNodesAndEvaluations(jep, node);
						}
						if (result != null && (result.equals(1.0d) || result.equals(Boolean.TRUE))) {
							// TODO Take a snapshot of the cluster at the time of the rule becoming
							// true and an index started, including the state of the indexes on the file
							// system and the other servers
							proceed = Boolean.TRUE;
						}
					} else {
						LOGGER.warn("Couldn't find the index context : " + proceedingJoinPoint);
					}
				}
			}
			if (proceed) {
				proceed(proceedingJoinPoint, indexContext.getIndexName(), "");
			}
		} catch (Throwable t) {
			LOGGER.error("Exception evaluating the rules : ", t);
		} finally {
			AtomicAction.unlock(lock);
		}
		LOGGER.info(Logging.getString("Rule intercepter proceeding : ", proceed, result, jep, predicate));
		return proceed;
	}

	protected void proceed(final ProceedingJoinPoint proceedingJoinPoint, final String indexName, final String indexableName) {
		long delay = 1;
		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
		executorService.schedule(new Runnable() {
			public void run() {
				// IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
				try {
					// clusterManager.setWorking(indexName, indexableName, Boolean.TRUE);
					proceedingJoinPoint.proceed();
				} catch (Throwable e) {
					LOGGER.error("Exception proceeding on join point : " + proceedingJoinPoint, e);
				} finally {
					// clusterManager.setWorking(indexName, indexableName, Boolean.FALSE);
				}
			}
		}, delay, TimeUnit.MILLISECONDS);
		executorService.shutdown();
	}

	protected void printNodesAndEvaluations(JEP jep, Node node) {
		try {
			LOGGER.info("Child node : " + node);
			Object childResult = jep.evaluate(node);
			LOGGER.info("           : " + childResult);
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node childNode = node.jjtGetChild(i);
				printNodesAndEvaluations(jep, childNode);
			}
		} catch (Exception e) {
			LOGGER.error("Exception printing the nodes : ", e);
		}
	}

}