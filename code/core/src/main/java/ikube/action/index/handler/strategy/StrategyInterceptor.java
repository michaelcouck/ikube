package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.List;

import org.apache.lucene.document.Document;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see IStrategyInterceptor
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

		IndexContext<?> indexContext = (IndexContext<?>) args[0];
		Indexable<?> indexable = (Indexable<?>) args[1];
		Document document = (Document) args[2];
		Object resource = args[3];

		List<IStrategy> strategies = indexable.getStrategies();
		if (strategies != null && !strategies.isEmpty()) {
			for (final IStrategy strategy : strategies) {
				mustProcess &= strategy.aroundProcess(indexContext, indexable, document, resource);
			}
		}

		return mustProcess ? proceedingJoinPoint.proceed(args) : mustProcess;
	}

}