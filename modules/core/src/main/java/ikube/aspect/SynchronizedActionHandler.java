package ikube.aspect;

import ikube.cluster.ILockManager;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class SynchronizedActionHandler implements ISynchronizedActionHandler {

	private PooledExecutor pooledExecuter = new PooledExecutor();

	private Logger logger;
	private ILockManager lockManager;

	public SynchronizedActionHandler() {
		this.logger = Logger.getLogger(this.getClass());
		pooledExecuter.createThreads(5);
	}

	@Override
	public synchronized Object execute(final ProceedingJoinPoint call) throws Throwable {
		try {
			// Check that we have the token. This waits for the token to be available
			boolean haveToken = lockManager.haveToken();
			while (!haveToken) {
				try {
					notifyAll();
					logger.debug("Waiting : " + call + ", " + Thread.currentThread().hashCode());
					wait(100);
					haveToken = lockManager.haveToken();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
			logger.debug("Proceeding : " + call + ", " + Thread.currentThread().hashCode());
			call.proceed();
//			pooledExecuter.execute(new Runnable() {
//				public void run() {
//					try {
//						call.proceed();
//					} catch (Throwable e) {
//						logger.error("Exception executing the action : " + call, e);
//					}
//				}
//			});
			return Boolean.TRUE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

	public void setLockManager(ILockManager lockManager) {
		this.lockManager = lockManager;
	}

}
