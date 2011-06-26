package ikube.action;

import ikube.ATest;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 02.06.11
 * @version 01.00
 */
public class DiskFullTest extends ATest {

	@SuppressWarnings("unused")
	private DiskFull diskFull;

	public DiskFullTest() {
		super(DiskFullTest.class);
	}

	@Before
	public void before() throws Exception {
		diskFull = new DiskFull();
		Mockit.setUpMocks();
	}

	@After
	public void after() throws Exception {
		Mockit.tearDownMocks();
	}

	@Test
	public void execute() throws Exception {
		// TODO The following:
		// We need to mock the FileSystemUtils#freeSpaceKb(drive) method
		// and return first a true, then a false. But we have to mock out the System#exit(int)
		// as well as this will close down the Jvm
	}

}