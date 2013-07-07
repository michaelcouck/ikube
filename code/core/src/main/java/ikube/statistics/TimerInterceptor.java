package ikube.statistics;

import ikube.toolkit.Timer;

import java.util.concurrent.atomic.AtomicLong;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public class TimerInterceptor implements ITimerInterceptor {

	static final Logger LOGGER = LoggerFactory.getLogger(TimerInterceptor.class);

	class TimedImpl implements Timer.Timed {

		Object result;
		ProceedingJoinPoint proceedingJoinPoint;

		void setProceedingJoinPoint(ProceedingJoinPoint proceedingJoinPoint) {
			this.proceedingJoinPoint = proceedingJoinPoint;
		}

		Object getResult() {
			return result;
		}

		@Override
		public void execute() {
			try {
				result = proceedingJoinPoint.proceed();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	private TimedImpl timedImpl;
	private AtomicLong everyNInvocations;
	private long printEveryNInvocations = 1000;

	public TimerInterceptor() {
		timedImpl = new TimedImpl();
		everyNInvocations = new AtomicLong(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object aroundProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		if (everyNInvocations.getAndIncrement() % printEveryNInvocations == 0) {
			timedImpl.setProceedingJoinPoint(proceedingJoinPoint);
			long duration = Timer.execute(timedImpl);
			LOGGER.info("Signature : " + proceedingJoinPoint.getSignature() + ", executed in : " + duration + " ms");
			return timedImpl.getResult();
		}
		return proceedingJoinPoint.proceed();
	}

}