package ikube.action;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Mailer;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;
import mockit.*;
import org.apache.commons.io.FileSystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 02-06-2011
 */
public class DiskFullTest extends AbstractTest {

    @MockClass(realClass = FileSystemUtils.class)
    public static class FileSystemUtilsMock {
        @Mock
        public static long freeSpaceKb(String path) throws IOException {
            return 0;
        }
    }

    /**
     * Class under test.
     */
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
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
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
            Mockit.tearDownMocks(FileSystemUtilsMock.class);
        }
    }

    @Test
    public void getLinuxRoot() {
        String root = diskFull.getNixRoot("/tmp");
        assertTrue(root.startsWith("/dev"));
        double iterationsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() {
                diskFull.getNixRoot("/media/sdb");
            }
        }, "Disk full check : ", 100, Boolean.TRUE);
        assertTrue(iterationsPerSecond > 10);
    }

}