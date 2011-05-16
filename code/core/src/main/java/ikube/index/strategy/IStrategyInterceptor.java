package ikube.index.strategy;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public interface IStrategyInterceptor {

	void executeStrategies(final ProceedingJoinPoint proceedingJoinPoint);

}
