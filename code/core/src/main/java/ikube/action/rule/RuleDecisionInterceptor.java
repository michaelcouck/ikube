package ikube.action.rule;

import ikube.action.IAction;
import ikube.model.IndexContext;

import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleDecisionInterceptor implements IRuleDecisionInterceptor {

	private static final String[] VARIABLES = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };

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

		logger.info("Intercepting : " + target);
		int index = 0;
		for (IRule<IndexContext> rule : classRules) {
			Object[] args = proceedingJoinPoint.getArgs();
			for (Object arg : args) {
				if (arg != null && IndexContext.class.isAssignableFrom(arg.getClass())) {
					boolean result = rule.evaluate((IndexContext) arg);
					if (logger.isDebugEnabled()) {
						logger.debug("Parameter : " + VARIABLES[index] + ", " + result);
					}
					jep.addVariable(VARIABLES[index], result);
					index++;
				}
			}
		}
		String predicate = ((IAction<?, ?>) target).getPredicate();
		Node node = jep.parse(predicate);
		Object result = jep.evaluate(node);
		logger.info("Result : " + result + ", " + jep + ", " + predicate);
		if (result == null || result.equals(0.0d) || result.equals(Boolean.FALSE)) {
			return Boolean.FALSE;
		}
		return proceedingJoinPoint.proceed();
	}

}
