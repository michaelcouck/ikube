package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileUtilitiesTest extends BaseTest {

	private File folder = new File(".");
	private File file = new File(folder, IConstants.READER_FILE_SUFFIX);
	private String[] stringPatterns = new String[] { IConstants.READER_FILE_SUFFIX };

	@Before
	public void before() {
		if (file.exists()) {
			file.delete();
		}
	}

	@After
	public void after() {
		if (file.exists()) {
			file.delete();
		}
	}

	@Test
	public void findFiles() throws Exception {
		assertFalse(file.exists());
		file.createNewFile();
		assertTrue(file.exists());

		File[] files = FileUtilities.findFiles(folder, stringPatterns);
		assertEquals(1, files.length);
		file.delete();

		files = FileUtilities.findFiles(folder, stringPatterns);
		assertEquals(0, files.length);
	}

	@Test
	public void findFilesRecursively() throws Exception {
		assertFalse(file.exists());
		file.createNewFile();
		assertTrue(file.exists());

		List<File> files = new ArrayList<File>();
		FileUtilities.findFilesRecursively(folder, stringPatterns, files);
		assertEquals(1, files.size());
		files.clear();

		FileUtilities.findFilesRecursively(folder, new String[] { ".xml" }, files);
		assertTrue(files.size() > 0);
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

		FileUtilities.deleteFiles(folder, stringPatterns);
		assertFalse(file.exists());
	}

	@Test
	public void findFile() {
		File file = FileUtilities.findFile(new File("."), new String[] { "mime-types.xml" });
		assertNotNull(file);
	}

}
