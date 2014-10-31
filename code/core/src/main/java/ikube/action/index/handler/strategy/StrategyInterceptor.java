package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.Timer;
import org.apache.lucene.document.Document;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Michael Couck
 * @version 01.00
 * @see IStrategyInterceptor
 * @since 27-12-2012
 */
public class StrategyInterceptor implements IStrategyInterceptor {

    static final Logger LOGGER = LoggerFactory.getLogger(StrategyInterceptor.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            boolean mustProceed = preProcess(proceedingJoinPoint);
            if (mustProceed) {
                return proceedingJoinPoint.proceed();
            }
            return Boolean.FALSE;
        } finally {
            postProcess(proceedingJoinPoint);
        }
    }

    private Boolean preProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // This method intercepts the handle... methods in the handlers. Each indexable will then define
        // strategies. These strategies will be executed and the accumulated category will be used to verify if the
        // method is to be executed or not
        final Object[] args = proceedingJoinPoint.getArgs();
        final AtomicBoolean mustProcess = new AtomicBoolean(Boolean.TRUE);
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                IndexContext indexContext = (IndexContext) args[0];
                Indexable indexable = (Indexable) args[1];
                Document document = (Document) args[2];
                Object resource = args[3];

                List<IStrategy> strategies = indexable.getStrategies();
                if (strategies != null && !strategies.isEmpty()) {
                    for (final IStrategy strategy : strategies) {
                        try {
                            mustProcess.set(strategy.aroundProcess(indexContext, indexable, document, resource));
                        } catch (final Exception e) {
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
        if (System.currentTimeMillis() % 10000 == 0) {
            LOGGER.info("Strategy chain duration : " + duration);
        }
        return mustProcess.get();
    }

    private void postProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // This method intercepts the handle... methods in the handlers. Each indexable will then define
        // strategies. These strategies will be executed and the accumulated category will be used to verify if the
        // method is to be executed or not
        final Object[] args = proceedingJoinPoint.getArgs();
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                IndexContext indexContext = (IndexContext) args[0];
                Indexable indexable = (Indexable) args[1];
                Document document = (Document) args[2];
                Object resource = args[3];

                List<IStrategy> strategies = indexable.getStrategies();
                if (strategies != null && !strategies.isEmpty()) {
                    for (final IStrategy strategy : strategies) {
                        try {
                            strategy.postProcess(indexContext, indexable, document, resource);
                        } catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        if (System.currentTimeMillis() % 10000 == 0) {
            LOGGER.info("Strategy chain duration : " + duration);
        }
    }

}