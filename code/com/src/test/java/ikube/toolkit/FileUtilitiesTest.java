package ikube.toolkit;

import ikube.AbstractTest;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class FileUtilitiesTest extends AbstractTest {

	private File file;
	private File dotFolder;
	private String[] stringPatterns;

	@Before
	public void before() {
		String fileName = "file.file";
		dotFolder = new File(".");
		file = new File(dotFolder, fileName);
		stringPatterns = new String[] { fileName };

		FileUtilities.deleteFile(new File("./common"), 1);
		FileUtilities.deleteFile(new File("./spring.xml"), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(file, 1);
		FileUtilities.deleteFile(new File("./common"));
		FileUtilities.deleteFile(new File("./indexes"));
		FileUtilities.deleteFile(new File("./spring.xml"));
	}

	@Test
	public void findFiles() throws Exception {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());

		File[] files = FileUtilities.findFiles(dotFolder, stringPatterns);
		int initialLength = files.length;
		assertTrue(initialLength >= 1);
		assertTrue(file.delete());

		files = FileUtilities.findFiles(dotFolder, stringPatterns);
		assertEquals(initialLength - 1, files.length);
	}

	@Test
	public void findFilesRecursively() throws Exception {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());

		List<File> files = FileUtilities.findFilesRecursively(dotFolder, new ArrayList<File>(), stringPatterns);
		assertTrue(files.size() >= 1);
		files.clear();

		files = FileUtilities.findFilesRecursively(dotFolder, files, ".xml");
		assertTrue(files.size() >= 1);

		List<File> properties = FileUtilities.findFilesRecursively(dotFolder, new ArrayList<File>(), "spring\\.properties");
		List<File> configurations = FileUtilities.findFilesRecursively(dotFolder, new ArrayList<File>(), "spring.*\\.xml");

		assertTrue(properties.size() > 0);
		assertTrue(configurations.size() > 0);
	}

	@Test
	public void deleteFile() throws Exception {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());

		FileUtilities.deleteFile(file, 1);
		assertFalse(file.exists());
	}

	@Test
	public void deleteFiles() throws Exception {
		assertFalse(file.exists());
		assertTrue(file.createNewFile());
		assertTrue(file.exists());

		FileUtilities.deleteFiles(dotFolder, stringPatterns);
		assertFalse(file.exists());
	}

	@Test
	public void findFile() {
		File file = FileUtilities.findFileRecursively(new File("."), "doctors.xml");
		assertNotNull(file);
	}

	@Test
	public void setContents() throws Exception {
		String data = "Michael Couck";
		File tempFile = FileUtilities.getFile("./indexes/data.dat", Boolean.FALSE);
		FileUtilities.setContents(tempFile.getAbsolutePath(), data.getBytes());
		assertTrue(tempFile.exists());
		assertTrue(tempFile.length() > 5);
	}

	@Test
	public void findDirectoryRecursively() {
		File file = FileUtilities.findDirectoryRecursively(new File("."), "data");
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
	}

	@Test
	public void findFileRecursivelyUp() {
		File folder = new File(".").getAbsoluteFile();
		File pomFile = FileUtilities.findFileRecursively(folder, 2, "mime-mapping.xml");
		assertNotNull(pomFile);
	}

	@Test
	public void findDirectoryRecursivelyUp() {
		File folder = new File(".").getAbsoluteFile();
		File textSentimentFolder = FileUtilities.findDirectoryRecursively(folder, 2, "txt_sentoken");
		assertNotNull(textSentimentFolder);
	}

	@Test
	public void getContents() throws IOException {
		HttpClient httpClient = getHttpClient();
		GetMethod getMethod = new GetMethod("http://www.google.com");
		try {
			httpClient.executeMethod(getMethod);
			InputStream inputStream = getMethod.getResponseBodyAsStream();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			FileUtilities.getContents(inputStream, byteArrayOutputStream, Integer.MAX_VALUE);
			assertTrue(!StringUtils.isEmpty(byteArrayOutputStream.toString()));
		} catch (UnknownHostException e) {
			// We ignore this exception as the machine could be offline
			logger.error("Machine offline?", e);
		}
	}

	/**
	 * NOTE: This tests needs to run in a directory where there is only one directory
	 * that is called ikube. For example it will not work in a directory where the structure is
	 * <pre>
	 *     Workspace
	 *          ikube
	 *          ikube-bck
	 * </pre>
	 * because it will look in both ikube directories and probably will not get the correct file.
	 */
	@Test
	public void relativeParent() {
		String dotFolderPath = FileUtilities.cleanFilePath(new File(".").getAbsolutePath());
		File dotFolder = new File(dotFolderPath);
		File relative = FileUtilities.relative(dotFolder, "../../");
		assertEquals(dotFolder.getParentFile().getParentFile(), relative);
	}

	private HttpClient getHttpClient() {
		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams();
		connectionManagerParams.setDefaultMaxConnectionsPerHost(10000);
		connectionManagerParams.setMaxTotalConnections(100000);
		connectionManagerParams.setStaleCheckingEnabled(true);
		connectionManagerParams.setTcpNoDelay(true);
		multiThreadedHttpConnectionManager.setParams(connectionManagerParams);
		HttpClientParams httpClientParams = new HttpClientParams();
		return new HttpClient(httpClientParams, multiThreadedHttpConnectionManager);
	}

}