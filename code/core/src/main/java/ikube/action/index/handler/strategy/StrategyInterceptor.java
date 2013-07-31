package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.Timer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

	private AtomicLong counter = new AtomicLong(0);

	/**
	 * {@inheritDoc}
	 */
	public Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// This method intercepts the handle... methods in the handlers. Each indexable will then define
		// strategies. These strategies will be executed and the accumulated category will be used to verify if the
		// method is to be executed or not
		final Object[] args = proceedingJoinPoint.getArgs();
		final AtomicBoolean mustProcess = new AtomicBoolean(Boolean.TRUE);
		long duration = Timer.execute(new Timer.Timed() {
			@Override
			public void execute() {

				IndexContext<?> indexContext = (IndexContext<?>) args[0];
				Indexable<?> indexable = (Indexable<?>) args[1];
				Document document = (Document) args[2];
				Object resource = args[3];

				List<IStrategy> strategies = indexable.getStrategies();
				if (strategies != null && !strategies.isEmpty()) {
					for (final IStrategy strategy : strategies) {
						try {
							mustProcess.set(strategy.aroundProcess(indexContext, indexable, document, resource));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						if (!mustProcess.get()) {
							LOGGER.info("Not proceeding : " + mustProcess + ", " + strategy + ", " + proceedingJoinPoint.getTarget());
							break;
						}
					}
				}
			}
		});
		if (counter.getAndIncrement() % 10000 == 0) {
			LOGGER.info("Strategy chain duration : " + duration);
		}
		return mustProcess.get() ? proceedingJoinPoint.proceed(args) : mustProcess.get();
	}

}