package ikube.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Ignore;
import org.junit.Test;

public class NasTest {

	@Test
	@Ignore
	public void nasWrite() throws Exception {
		String string = "Michael Couck";
		String[] nasFiles = { //
		"/media/nas/xfs-one/directory/foo.txt", //
				"/media/nas/xfs-two/directory/foo.txt", //
				"/media/nas/xfs-three/directory/foo.txt", //
				"/media/nas/xfs-four/directory/foo.txt", //
				"/media/nas/xfs-five/directory/foo.txt" };
		for (String nasFile : nasFiles) {
			File file = FileUtilities.getFile(nasFile, Boolean.FALSE);
			assertNotNull(file);
			assertTrue(file.exists());

			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(string.getBytes());
			fileOutputStream.flush();
			fileOutputStream.close();

			// FileUtils.writeStringToFile(file, string);
			FileUtilities.setContents(file.getAbsolutePath(), string.getBytes());

			String content = FileUtilities.getContents(file, IConstants.ENCODING);
			assertEquals(string, content);
		}
	}

}