package ikube.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.Ordered;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface ISynchronizedActionHandler extends Ordered {

	public Object execute(ProceedingJoinPoint call) throws Throwable;

}
