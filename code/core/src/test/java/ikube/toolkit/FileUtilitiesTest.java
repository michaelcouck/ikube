package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class FileUtilitiesTest extends ATest {

	private File file;
	private File dotFolder;
	private File indexFolderOne;
	private File indexFolderTwo;
	private File indexFolderThree;
	private String[] stringPatterns;

	@Before
	public void before() {
		dotFolder = new File(".");
		file = new File(dotFolder, IConstants.READER_FILE_SUFFIX);
		String fileUtilitiesTestIndexdirectory = "fileUtilitiesTestIndexdirectory";
		indexFolderOne = FileUtilities.getFile("./" + fileUtilitiesTestIndexdirectory + "/1234567889/127.0.0.1", Boolean.TRUE);
		indexFolderTwo = FileUtilities.getFile("./" + fileUtilitiesTestIndexdirectory + "/1234567891/127.0.0.2", Boolean.TRUE);
		indexFolderThree = FileUtilities.getFile("./" + fileUtilitiesTestIndexdirectory + "/1234567890/127.0.0.3", Boolean.TRUE);
		stringPatterns = new String[] { IConstants.READER_FILE_SUFFIX };
	}

	@After
	public void after() {
		FileUtilities.deleteFile(file, 1);
		if (indexFolderOne != null && indexFolderOne.getParentFile().getParentFile().exists()) {
			FileUtilities.deleteFile(indexFolderOne.getParentFile().getParentFile(), 1);
		}
	}

	@Test
	public void findFiles() throws Exception {
		assertFalse(file.exists());
		file.createNewFile();
		assertTrue(file.exists());

		File[] files = FileUtilities.findFiles(dotFolder, stringPatterns);
		int initialLength = files.length;
		assertTrue(initialLength >= 1);
		file.delete();

		files = FileUtilities.findFiles(dotFolder, stringPatterns);
		assertEquals(initialLength - 1, files.length);
	}

	@Test
	public void findFilesRecursively() throws Exception {
		assertFalse(file.exists());
		file.createNewFile();
		assertTrue(file.exists());

		List<File> files = FileUtilities.findFilesRecursively(dotFolder, stringPatterns, new ArrayList<File>());
		int initialLength = files.size();
		assertTrue(initialLength >= 1);
		files.clear();

		files = FileUtilities.findFilesRecursively(dotFolder, new String[] { ".xml" }, files);
		initialLength = files.size();
		assertTrue(initialLength >= 1);
	}

	@Test
	public void deleteFile() throws Exception {
		assertFalse(file.exists());
		file.createNewFile();
		assertTrue(file.exists());

		FileUtilities.deleteFile(file, 1);
		assertFalse(file.exists());
	}

	@Test
	public void deleteFiles() throws Exception {
		assertFalse(file.exists());
		file.createNewFile();
		assertTrue(file.exists());

		FileUtilities.deleteFiles(dotFolder, stringPatterns);
		assertFalse(file.exists());
	}

	@Test
	public void findFile() {
		File file = FileUtilities.findFile(new File("."), new String[] { "mime-types.xml" });
		assertNotNull(file);
	}

	@Test
	public void setContents() throws Exception {
		String data = "Michael Couck";
		File tempFile = File.createTempFile("temp", ".file");
		FileUtilities.setContents(tempFile.getAbsolutePath(), data.getBytes());
		assertTrue(tempFile.exists());
		assertTrue(tempFile.length() > 5);
	}

	@Test
	public void getLatestIndexDirectoryFileFile() {
		// File, File
		File latest = FileUtilities.getLatestIndexDirectory(indexFolderOne.getParentFile().getParentFile(), null);
		assertEquals(indexFolderTwo.getParentFile(), latest);
		latest = FileUtilities.getLatestIndexDirectory(indexFolderTwo.getParentFile().getParentFile(), null);
		assertEquals(indexFolderTwo.getParentFile(), latest);
		latest = FileUtilities.getLatestIndexDirectory(indexFolderThree.getParentFile().getParentFile(), null);
		assertEquals(indexFolderTwo.getParentFile(), latest);
	}

	@Test
	public void getLatestIndexDirectoryString() {
		// String
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexFolderOne.getParentFile().getParentFile().getAbsolutePath());
		logger.info("Latest : " + latestIndexDirectory);
		assertEquals(indexFolderTwo.getParentFile().getName(), latestIndexDirectory.getName());
	}

	@Test
	public void getOldestIndexDirectoryFilefile() {
		// File, File
		File latest = FileUtilities.getNewestIndexDirectory(indexFolderOne.getParentFile(), indexFolderTwo.getParentFile());
		assertEquals(indexFolderTwo.getParentFile(), latest);
	}

}
