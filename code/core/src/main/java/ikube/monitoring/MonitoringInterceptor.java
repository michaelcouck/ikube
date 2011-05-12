package ikube.monitoring;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @see IMonitoringInterceptor
 * @author Michael Couck
 * @since 08.05.2011
 * @version 01.00
 */
public class MonitoringInterceptor implements IMonitoringInterceptor {

	@SuppressWarnings("unused")
	private static final transient Logger LOGGER = Logger.getLogger(MonitoringInterceptor.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object indexingPerformance(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object searchingPerformance(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		return null;
	}

}