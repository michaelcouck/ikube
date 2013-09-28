package ikube.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.Mailer;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;

import java.io.IOException;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.commons.io.FileSystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 02.06.11
 * @version 01.00
 */
public class DiskFullTest extends AbstractTest {

	@MockClass(realClass = FileSystemUtils.class)
	public static class FileSystemUtilsMock {
		@Mock
		public static long freeSpaceKb(String path) throws IOException {
			return 0;
		}
	}

	/** Class under test. */
	private DiskFull diskFull;

	@Cascading
	private Mailer mailer;
	@Cascading
	private ThreadUtilities threadUtilities;

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
		Deencapsulation.setField(diskFull, mailer);
		Deencapsulation.setField(diskFull, clusterManager);
		boolean theDiskIsFull = diskFull.execute(indexContext);
		assertFalse("The disk should never be too full : ", theDiskIsFull);
		try {
			Mockit.setUpMocks(FileSystemUtilsMock.class);
			theDiskIsFull = diskFull.execute(indexContext);
			assertTrue("The should be full now : ", theDiskIsFull);
		} finally {
			Mockit.tearDownMocks();
		}
	}

	@Test
	public void getLinuxRoot() {
		String root = diskFull.getNixRoot("/mnt/sdb/backup");
		assertEquals("/dev/sdb1", root);
		root = diskFull.getNixRoot("/tmp");
		assertEquals("/dev/sda1", root);
		double iterationsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() {
				diskFull.getNixRoot("/mnt/sdb/backup");
			}
		}, "Disk full check : ", 100, Boolean.TRUE);
		assertTrue(iterationsPerSecond > 10);
	}

}