package ikube.action;

import static org.junit.Assert.assertFalse;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import mockit.Deencapsulation;
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

	private DiskFull diskFull;

	public DiskFullTest() {
		super(DiskFullTest.class);
	}

	@Before
	public void before() throws Exception {
		diskFull = new DiskFull();
		Deencapsulation.setField(diskFull, clusterManager);
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
	}

	@After
	public void after() throws Exception {
		Mockit.tearDownMocks();
	}

	@Test
	public void execute() throws Exception {
		boolean diskFull = this.diskFull.execute(indexContext);
		assertFalse("The disk should never be too full : ", diskFull);
	}

}