package ikube.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

public class NasTest {

	@Test
	@Ignore
	public void nasWrite() throws Exception {
		String string = "Michael Couck";
		File file = FileUtilities.getFile("/media/nasone/text.txt", Boolean.FALSE);
		assertNotNull(file);
		assertTrue(file.exists());

		// FileOutputStream fileOutputStream = new FileOutputStream(file);
		// fileOutputStream.write(string.getBytes());
		// fileOutputStream.flush();
		// fileOutputStream.close();

		// FileUtils.writeStringToFile(file, string);
		FileUtilities.setContents(file.getAbsolutePath(), string.getBytes());

		String content = FileUtilities.getContents(file, IConstants.ENCODING);
		assertEquals(string, content);
	}

}