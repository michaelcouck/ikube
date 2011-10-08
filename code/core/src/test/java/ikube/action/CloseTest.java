package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;

import java.io.IOException;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class CloseTest extends ATest {

	private Close close;

	public CloseTest() {
		super(CloseTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		close = new Close();
	}
	
	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void execute() throws IOException {
		boolean closed = close.execute(indexContext);
		assertTrue("The index was open and it should have been closed in the action : ", closed);
	}

}