package ikube.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class SynchronizationWebServiceTest extends BaseTest {

	private SynchronizationWebService synchronizationWebService;
	private String baseDirectory = "./index";
	private String latestDirectory = Long.toString(System.currentTimeMillis());
	private String serverDirectory;
	private String contextDirectory = indexContext.getName();
	private String file = "dummy.cfs";

	@Before
	public void before() throws Exception {
		synchronizationWebService = ApplicationContextManager.getBean(SynchronizationWebService.class);
		serverDirectory = InetAddress.getLocalHost().getHostAddress();
	}

	@After
	public void after() {
		File baseDirectoryFolder = new File(baseDirectory);
		FileUtilities.deleteFile(baseDirectoryFolder, 1);
	}

	@Test
	public void wantsFile() {
		boolean wantsFile = synchronizationWebService.wantsFile(baseDirectory, latestDirectory, serverDirectory, contextDirectory, file);
		assertTrue(wantsFile);
	}

	@Test
	public void getIndexFile() {
		File indexFile = synchronizationWebService.getIndexFile(baseDirectory, latestDirectory, serverDirectory, contextDirectory, file);
		assertNotNull(indexFile);
		assertFalse(indexFile.exists());
	}

	@Test
	public void writeIndexFile() {
		byte[] bytes = "Some data".getBytes();
		boolean wroteData = synchronizationWebService.writeIndexFile(baseDirectory, latestDirectory, serverDirectory, contextDirectory,
				file, bytes);
		assertTrue(wroteData);
	}

}
