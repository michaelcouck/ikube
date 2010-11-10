package ikube.toolkit;

import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.apache.log4j.Logger;

/**
 * During the testing with object databases for persistence it looked like the threads were getting locked, and in fact even this thread
 * stopped so finally oodb couldn't be used. Still no resolution on that.
 *
 * Note: Well in fact the lock was on one of the datasources. As it turns out the C3P0 datasource locks the thread while it waits for more
 * connections. Of course if the connections are not closed then there will never be any more after the pool is depleted. So all is well
 * with using the Oodb. Had to investigate with VisualVm, great tool.
 *
 * This thread iterates over the threads that can be locked and prints out the info on them, if there are any of course.
 *
 * @author Michael Couck
 * @since 15.10.09
 * @version 01.00
 */
public class ThreadDeadLockDetector implements IListener {

	private Logger logger = Logger.getLogger(ThreadDeadLockDetector.class);

	public ThreadDeadLockDetector() {
		ListenerManager.addListener(this);
	}

	@Override
	public void handleNotification(Event event) {
		logger.info("Deadlock :" + event);
		if (event.getType().equals(Event.DEAD_LOCK)) {
			ThreadMXBean bean = ManagementFactory.getThreadMXBean();
			logger.error("Thread deadlock bean : " + bean);
			long[] threadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.
			if (threadIds != null) {
				ThreadInfo[] infos = bean.getThreadInfo(threadIds);
				for (ThreadInfo info : infos) {
					StackTraceElement[] stackTraceElements = info.getStackTrace();
					logger.error("Thread locked : " + info.getLockOwnerName());
					for (StackTraceElement stackTraceElement : stackTraceElements) {
						logger.error(stackTraceElement.getClassName() + ":" + stackTraceElement.getMethodName() + ":"
								+ stackTraceElement.getLineNumber());
					}
				}
			}
		}
	}

}
