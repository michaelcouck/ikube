package ikube.cluster;

import static org.junit.Assert.*;

import org.junit.Test;

import ikube.ATest;
import ikube.IConstants;

public class AtomicActionTest extends ATest {

	public AtomicActionTest() {
		super(AtomicActionTest.class);
	}

	@Test
	public void executeAction() {
		boolean result = AtomicAction.executeAction(IConstants.SERVER_LOCK, new AtomicAction() {
			@Override
			public boolean execute() {
				return Boolean.TRUE;
			}
		});
		assertTrue("Should be executed : ", result);
		AtomicAction.lock(IConstants.SERVER_LOCK);
		result = AtomicAction.executeAction(IConstants.SERVER_LOCK, new AtomicAction() {
			@Override
			public boolean execute() {
				return Boolean.TRUE;
			}
		});
		assertTrue("Should be executed : ", result);
	}

}
