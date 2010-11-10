package ikube.toolkit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.util.StopWatch;

public class ProfilingMethodInterceptor implements MethodInterceptor {

	private Logger logger;
	private StopWatch stopWatch = new StopWatch();

	public ProfilingMethodInterceptor() {
		this.logger = Logger.getLogger(this.getClass());
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		logger.info("Invocation : " + invocation.getMethod().getName());
		Object value = null;
		try {
			stopWatch.start();
			value = invocation.proceed();
		} catch (Exception e) {
			logger.error("Exception in the profiling advice, exceptions are expensive : ", e);
		} finally {
			stopWatch.stop();
		}
		logger.info("Stop watch : " + stopWatch.prettyPrint());
		return value;
	}

}
