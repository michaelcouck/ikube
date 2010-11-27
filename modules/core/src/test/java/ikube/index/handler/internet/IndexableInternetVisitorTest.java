package ikube.index.handler.internet;

import ikube.BaseTest;
import ikube.model.IndexableInternet;
import ikube.toolkit.ApplicationContextManager;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Ignore
public class IndexableInternetVisitorTest extends BaseTest {

	@Test
	public void visit() throws Exception {
		indexContext.setIndexWriter(indexWriter);
		IndexableInternet indexableInternet = ApplicationContextManager.getBean("oki");
		IndexableInternetHandler indexableInternetVisitor = ApplicationContextManager.getBean(IndexableInternetHandler.class);
		indexableInternetVisitor.handle(indexContext, indexableInternet);
	}

	// @Test
	public void waitForResources() {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 3; i++) {
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(3000);
						waitForThread();
					} catch (InterruptedException e) {
						logger.error("", e);
					}
				}
			}, Integer.toString(i));
			threads.add(thread);
			thread.start();
		}
		while (true) {
			for (Thread thread : threads) {
				logger.debug(thread + ", " + thread.getState());
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
		}
	}

	protected synchronized void waitForThread() {
		try {
			wait();
		} catch (InterruptedException e) {
			logger.error("", e);
		} finally {
			notifyAll();
		}
	}

}