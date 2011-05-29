package ikube.action.rule;

import ikube.action.IAction;
import ikube.model.IndexContext;
import ikube.toolkit.Logging;

import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;

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
		Object target = proceedingJoinPoint.getTarget();
		if (!IAction.class.isAssignableFrom(target.getClass())) {
			LOGGER.warn("Can't intercept non action class, proceeding : " + target);
			return proceedingJoinPoint.proceed();
		}

		// Get the rules associated with this action
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<IRule<IndexContext>> classRules = ((IAction) target).getRules();
		if (classRules == null) {
			LOGGER.warn("No rules defined for, proceeding : " + target);
			return proceedingJoinPoint.proceed();
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Intercepting : " + target);
		}
		JEP jep = new JEP();
		for (IRule<IndexContext> rule : classRules) {
			Object[] args = proceedingJoinPoint.getArgs();
			for (Object arg : args) {
				if (arg != null && IndexContext.class.isAssignableFrom(arg.getClass())) {
					boolean result = rule.evaluate((IndexContext) arg);
					String parameter = rule.getClass().getSimpleName();
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(Logging.getString("Rule : ", rule, ", parameter : ", parameter, ", result : ", result));
					}
					jep.addVariable(parameter, result);
				}
			}
		}
		String predicate = ((IAction<?, ?>) target).getRuleExpression();
		jep.parseExpression(predicate);
		if (jep.hasError()) {
			LOGGER.warn("Exception in Jep expression : " + jep.getErrorInfo());
			LOGGER.warn("Symbol table : " + jep.getSymbolTable());
		}
		Object result = jep.getValueAsObject();
		if (result == null) {
			result = jep.getValue();
		}
		if (result == null || result.equals(0.0d) || result.equals(Boolean.FALSE)) {
			LOGGER.info(Logging.getString("Not proceeding: ", result, jep, predicate));
			return Boolean.FALSE;
		}
		if (LOGGER.isInfoEnabled()) {
			// Node node = jep.getTopNode();
			// printNodesAndEvaluations(jep, node);
		}
		if (result.equals(1.0d) || result.equals(Boolean.TRUE)) {
			LOGGER.info(Logging.getString("Proceeding: ", result, jep, predicate));
			return proceedingJoinPoint.proceed();
		}
		LOGGER.info(Logging.getString("Not proceeding: ", result, jep, predicate));
		return Boolean.FALSE;
	}

	@SuppressWarnings("unused")
	private void printNodesAndEvaluations(JEP jep, Node node) {
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