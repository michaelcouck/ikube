package ikube.index.handler;

import ikube.model.Indexable;

import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the interceptor for the file system and data base handlers. Essentially this interceptor will execute strategies on the handlers
 * before processing and based on the result allow the handler to proceed to index the resource or not. This facilitates delta indexing and
 * adding data to the document before comitting the data to the index, like adding a file while processing the database.
 * 
 * @author Michael Couck
 * @since 27.12.12
 * @version 01.00
 */
// @Aspect
public class StrategyInterceptor implements IStrategyInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(StrategyInterceptor.class);

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	// @Around(IStrategyInterceptor.AROUND_EXPRESSION)
	public Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// This method intercepts the handle... methods in the handlers. Each indexable will then define
		// strategies. These strategies will be executed and the accumulated result will be used to verify if the
		// method is to be executed or not
		LOGGER.error("Strategy interceptor : ");
		boolean mustProcess = Boolean.TRUE;
		Object[] args = proceedingJoinPoint.getArgs();
		if (args != null && args.length > 0) {
			LOGGER.error("Args : " + Arrays.deepToString(args));
			for (final Object arg : args) {
				if (arg == null) {
					continue;
				}
				if (Indexable.class.isAssignableFrom(arg.getClass())) {
					List<IStrategy> strategies = ((Indexable) arg).getStrategies();
					if (strategies != null && !strategies.isEmpty()) {
						for (final IStrategy strategy : strategies) {
							boolean preProcess = strategy.preProcess(args);
							LOGGER.error("Strategy : " + strategy + ", " + preProcess);
							mustProcess &= preProcess;
						}
					}
				}
			}
		}
		LOGGER.error("Processing : " + mustProcess);
		if (mustProcess) {
			return proceedingJoinPoint.proceed(args);
		}
		return null;
	}

}