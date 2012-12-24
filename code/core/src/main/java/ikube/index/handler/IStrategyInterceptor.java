package ikube.index.handler;

import org.aspectj.lang.ProceedingJoinPoint;

public interface IStrategyInterceptor {

	Object preProcess(final ProceedingJoinPoint call) throws Throwable;

}
