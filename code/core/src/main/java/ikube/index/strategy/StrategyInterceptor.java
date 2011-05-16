package ikube.index.strategy;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class StrategyInterceptor implements IStrategyInterceptor {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(StrategyInterceptor.class);

	@Override
	public void executeStrategies(ProceedingJoinPoint proceedingJoinPoint) {
	}

}