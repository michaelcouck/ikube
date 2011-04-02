package ikube.action.rule;

import ikube.action.IAction;
import ikube.model.IndexContext;
import ikube.toolkit.Logging;

import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleInterceptor implements IRuleInterceptor {

	private static final transient Logger LOGGER = Logger.getLogger(RuleInterceptor.class);

	@Override
	public Object decide(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
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

		LOGGER.info("Intercepting : " + target);
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
		String predicate = ((IAction<?, ?>) target).getPredicate();
		jep.parseExpression(predicate);
		if (jep.hasError()) {
			LOGGER.warn("Exception in Jep expression : " + jep.getErrorInfo());
			LOGGER.warn("Symbol table : " + jep.getSymbolTable());
		}
		Object result = jep.getValueAsObject();
		LOGGER.info(Logging.getString("Result : ", result, jep, predicate));
		if (result == null) {
			result = jep.getValue();
		}
		if (result == null || result.equals(0.0d) || result.equals(Boolean.FALSE)) {
			LOGGER.debug(Logging.getString("Not proceeding: "));
			return Boolean.FALSE;
		}
		LOGGER.debug(Logging.getString("Proceeding: "));
		return proceedingJoinPoint.proceed();
	}

}
