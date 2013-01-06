package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
@Aspect
public class StrategyInterceptor implements IStrategyInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(StrategyInterceptor.class);

	@Pointcut(IStrategyInterceptor.POINTCUT_EXPRESSION)
	public void pointcut() {
		LOGGER.info("Point cut : ");
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Around(IStrategyInterceptor.AROUND_EXPRESSION)
	public Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// This method intercepts the handle... methods in the handlers. Each indexable will then define
		// strategies. These strategies will be executed and the accumulated result will be used to verify if the
		// method is to be executed or not
		boolean mustProcess = Boolean.TRUE;
		Object[] args = proceedingJoinPoint.getArgs();
		// LOGGER.info("Args : " + Arrays.deepToString(args));
		if (args != null && args.length > 0) {
			for (final Object arg : args) {
				if (arg == null) {
					continue;
				}
				Class<?> indexableClass = arg.getClass();
				// LOGGER.error("Is indexable : " + Indexable.class.isAssignableFrom(indexableClass) + ", " + indexableClass + ", " + arg);
				if (!IndexContext.class.isAssignableFrom(indexableClass) && Indexable.class.isAssignableFrom(indexableClass)) {
					List<IStrategy> strategies = ((Indexable) arg).getStrategies();
					// LOGGER.error("Strategies : " + strategies);
					if (strategies != null && !strategies.isEmpty()) {
						for (final IStrategy strategy : strategies) {
							boolean preProcess = strategy.preProcess(args);
							// LOGGER.error("Strategy : " + strategy + ", " + preProcess);
							mustProcess &= preProcess;
						}
						// LOGGER.error("Processing : " + mustProcess + ", " + Arrays.deepToString(args));
						break;
					}
				}
			}
		}
		LOGGER.error("Must process : " + mustProcess);
		if (mustProcess) {
			return proceedingJoinPoint.proceed(args);
		}
		return null;
	}

}