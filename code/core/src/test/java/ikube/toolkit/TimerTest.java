package ikube.toolkit;

import ikube.AbstractTest;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 07.01.2014
 */
public class TimerTest extends AbstractTest {

	@Test
	public void actionInterceptor() throws Exception {
		double duration = Timer.execute(new Timer.Timed() {
			@Override
			public void execute() {
				ThreadUtilities.sleep(1);
			}
		});
		assertTrue(duration > 1 && duration < 2);
	}
}