package ikube.action.rule;

import ikube.model.IndexContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class RuleDecisionInterceptor implements IRuleDecisionInterceptor {

	private Logger logger = Logger.getLogger(this.getClass());
	private Map<Object, List<IRule<IndexContext>>> rules;

	@Override
	public Object decide(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		Object target = proceedingJoinPoint.getTarget();
		logger.info("Intercepting : " + target);
		// Get the rules associated with this action
		List<IRule<IndexContext>> classRules = this.rules.get(target);
		if (classRules == null) {
			return proceedingJoinPoint.proceed();
		}
		// Execute the rules, if one returns false then we don't execute the action
		for (IRule<IndexContext> rule : classRules) {
			Object[] args = proceedingJoinPoint.getArgs();
			for (Object arg : args) {
				if (IndexContext.class.isAssignableFrom(arg.getClass())) {
					boolean result = rule.evaluate((IndexContext) arg);
					if (!result) {
						return Boolean.FALSE;
					}
				}
			}
		}
		return proceedingJoinPoint.proceed();
	}

	public Map<Object, List<IRule<IndexContext>>> getRules() {
		return Collections.unmodifiableMap(rules);
	}

	public void setRules(Map<Object, List<IRule<IndexContext>>> rules) {
		this.rules = rules;
	}

}
