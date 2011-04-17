package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.ATest;

import java.io.IOException;

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
		close = new Close();
	}

	@Test
	public void execute() throws IOException {
		boolean closed = close.execute(INDEX_CONTEXT);
		assertTrue("The index was open and it should have been closed in the action : ", closed);
	}

}