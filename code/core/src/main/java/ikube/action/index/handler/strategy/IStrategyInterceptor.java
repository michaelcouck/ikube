package ikube.action.index.handler.strategy;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Michael Couck
 * @since 27.12.12
 * @version 01.00
 */
public interface IStrategyInterceptor {

	String AROUND_EXPRESSION = "execution(* ikube.index.handler.filesystem.IndexableFilesystemHandler.handleFile(..))";
	String POINTCUT_EXPRESSION = "execution(* ikube.index.handler.filesystem.IndexableFilesystemHandler.handleFile(..))";

	Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable;

}
