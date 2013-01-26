package ikube.interceptor;

import ikube.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.List;

import org.apache.lucene.document.Document;
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
public class StrategyInterceptor implements IStrategyInterceptor {

	static final Logger LOGGER = LoggerFactory.getLogger(StrategyInterceptor.class);

	/**
	 * {@inheritDoc}
	 */
	public Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// This method intercepts the handle... methods in the handlers. Each indexable will then define
		// strategies. These strategies will be executed and the accumulated result will be used to verify if the
		// method is to be executed or not
		boolean mustProcess = Boolean.TRUE;
		Object[] args = proceedingJoinPoint.getArgs();
		// LOGGER.info("Args : " + args + ", " + proceedingJoinPoint.getSignature());

		final IndexContext<?> indexContext = (IndexContext<?>) args[0];
		final Indexable<?> indexable = (Indexable<?>) args[1];
		final Document document = (Document) args[2];
		final Object resource = args[3];

		List<IStrategy> strategies = indexable.getStrategies();
		// LOGGER.error("Strategies : " + strategies);
		if (strategies != null && !strategies.isEmpty()) {
			for (final IStrategy strategy : strategies) {
				boolean aroundProcess = strategy.aroundProcess(indexContext, indexable, document, resource);
				// LOGGER.error("Strategy : " + strategy + ", " + aroundProcess);
				mustProcess &= aroundProcess;
			}
		}

		// LOGGER.error("Must process : " + mustProcess);
		if (mustProcess) {
			return proceedingJoinPoint.proceed(args);
		}
		return null;
	}

}