package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.listener.ListenerManager;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.model.IndexContext;

import java.io.IOException;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.commons.io.FileSystemUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 02.06.11
 * @version 01.00
 */
public class DiskFullTest extends ATest {

	@MockClass(realClass = FileSystemUtils.class)
	public static class FileSystemUtilsMock {
		@Mock
		public static long freeSpaceKb(String path) throws IOException {
			return 0;
		}
	}

	private DiskFull diskFull;

	public DiskFullTest() {
		super(DiskFullTest.class);
	}

	@Before
	public void before() throws Exception {
		diskFull = Mockito.mock(DiskFull.class);
		Logger logger = Mockito.mock(Logger.class);
		Mockito.when(diskFull.execute(Mockito.any(IndexContext.class))).thenCallRealMethod();
		Mockito.when(diskFull.internalExecute(Mockito.any(IndexContext.class))).thenCallRealMethod();
		Deencapsulation.setField(diskFull, logger);
		Deencapsulation.setField(diskFull, clusterManager);
		Deencapsulation.setField(diskFull, Mockito.mock(ListenerManager.class));
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

		try {
			Mockit.setUpMocks(FileSystemUtilsMock.class);
			diskFull = this.diskFull.execute(indexContext);
			assertTrue("The should be fullnow : ", diskFull);
		} finally {
			Mockit.tearDownMocks(FileSystemUtilsMock.class);
		}

	}

}