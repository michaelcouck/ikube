package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;

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
public class FileUtilitiesTest extends AbstractTest {

	private File file;
	private File dotFolder;
	private File indexFolderOne;
	private String[] stringPatterns;

	@Before
	public void before() {
		String fileName = "file.file";
		dotFolder = new File(".");
		file = new File(dotFolder, fileName);
		String fileUtilitiesTestIndexdirectory = "fileUtilitiesTestIndexdirectory";
		indexFolderOne = FileUtilities.getFile("./" + fileUtilitiesTestIndexdirectory + "/1234567889/127.0.0.1", Boolean.TRUE);
		stringPatterns = new String[] { fileName };

		FileUtilities.deleteFile(new File("./common"), 1);
		FileUtilities.deleteFile(new File("./spring.xml"), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(file, 1);
		if (indexFolderOne != null && indexFolderOne.getParentFile().getParentFile().exists()) {
			FileUtilities.deleteFile(indexFolderOne.getParentFile().getParentFile(), 1);
		}
		FileUtilities.deleteFile(new File("./common"), 1);
		FileUtilities.deleteFile(new File("./spring.xml"), 1);
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
		int initialLength = files.size();
		assertTrue(initialLength >= 1);
		files.clear();

		files = FileUtilities.findFilesRecursively(dotFolder, files, ".xml");
		initialLength = files.size();
		assertTrue(initialLength >= 1);
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
		logger.info("Data directory : " + file.getAbsolutePath());
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(file.isDirectory());
	}
	
	@Test
	public void findFileRecursivelyUp() {
		File folder = new File(".").getAbsoluteFile();
		folder = FileUtilities.findFileRecursively(folder, "wiki");
		logger.info("Folder : " + folder.getAbsolutePath());
		File pomFile = FileUtilities.findFileRecursively(folder, 5, "pom.xml");
		assertNotNull(pomFile);
	}

}