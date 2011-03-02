package ikube.action.rule;

import ikube.action.IAction;
import ikube.logging.Logging;
import ikube.model.IndexContext;

import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleDecisionInterceptor implements IRuleDecisionInterceptor {

	private Logger logger = Logger.getLogger(this.getClass());
	private JEP jep = new JEP();

	@Override
	public Object decide(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object target = proceedingJoinPoint.getTarget();
		if (!IAction.class.isAssignableFrom(target.getClass())) {
			logger.warn("Can't intercept non action class, proceeding : " + target);
			return proceedingJoinPoint.proceed();
		}

		// Get the rules associated with this action
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<IRule<IndexContext>> classRules = ((IAction) target).getRules();
		if (classRules == null) {
			logger.warn("No rules defined for, proceeding : " + target);
			return proceedingJoinPoint.proceed();
		}

		logger.debug("Intercepting : " + target);
		int index = 0;
		for (IRule<IndexContext> rule : classRules) {
			Object[] args = proceedingJoinPoint.getArgs();
			for (Object arg : args) {
				if (arg != null && IndexContext.class.isAssignableFrom(arg.getClass())) {
					boolean result = rule.evaluate((IndexContext) arg);
					String parameter = rule.getClass().getSimpleName();
					if (logger.isDebugEnabled()) {
						logger.info(Logging.getString("Parameter : ", parameter, result));
					}
					jep.addVariableAsObject(parameter, result);
					index++;
				}
			}
		}
		String predicate = ((IAction<?, ?>) target).getPredicate();
		jep.parseExpression(predicate);
		Object result = jep.getValueAsObject();
		if (logger.isDebugEnabled()) {
			logger.debug(Logging.getString("Result : ", result, jep, predicate));
		}
		if (result == null || result.equals(0.0d) || result.equals(Boolean.FALSE)) {
			return Boolean.FALSE;
		}
		return proceedingJoinPoint.proceed();
	}

}
