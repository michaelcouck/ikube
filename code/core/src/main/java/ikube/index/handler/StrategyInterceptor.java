package ikube.index.handler;

import ikube.model.Indexable;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;

public class StrategyInterceptor implements IStrategyInterceptor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object preProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// This method intercepts the handle... methods in the handlers. Each indexable will then define
		// strategies. These strategies will be executed and the accumulated result will be used to verify if the
		// method is to be executed or not
		boolean mustProcess = Boolean.TRUE;
		Object[] args = proceedingJoinPoint.getArgs();
		for (final Object arg : args) {
			if (arg == null) {
				continue;
			}
			if (Indexable.class.isAssignableFrom(arg.getClass())) {
				List<IStrategy> strategies = ((Indexable) arg).getStrategies();
				if (strategies != null && !strategies.isEmpty()) {
					for (final IStrategy strategy : strategies) {
						mustProcess &= strategy.preProcess(args);
					}
				}
			}
		}
		if (mustProcess) {
			return proceedingJoinPoint.proceed(args);
		}
		return null;
	}

}
