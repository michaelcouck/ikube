package ikube.toolkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.Constants;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Ignore;
import org.junit.Test;
/**
 * @author Michael Couck
 * @since 08-02-2013
 * @version 01.00
 */
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
			File file = FILE.getFile(nasFile, Boolean.FALSE);
			assertNotNull(file);
			assertTrue(file.exists());

			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(string.getBytes());
			fileOutputStream.flush();
			fileOutputStream.close();

			// FileUtils.writeStringToFile(file, string);
			FILE.setContents(file.getAbsolutePath(), string.getBytes());

			String content = FILE.getContents(file, Constants.ENCODING);
			assertEquals(string, content);
		}
	}

}