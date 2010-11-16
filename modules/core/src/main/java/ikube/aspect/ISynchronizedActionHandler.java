package ikube.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.Ordered;

public interface ISynchronizedActionHandler extends Ordered {

	public Object execute(ProceedingJoinPoint call) throws Throwable;

}
