package ikube.action.index.parse.mime;

import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 10.02.10
 * @version 01.00
 */
public class MimeTypesTest extends AbstractTest {

	@Test
	public void getMimeTypesFromName() {
		MimeType mimeType = MimeTypes.getMimeTypeFromName("text/html");
		assertNotNull(mimeType);
		mimeType = MimeTypes.getMimeTypeFromName("index.html");
		assertNotNull(mimeType);
		mimeType = MimeTypes.getMimeTypeFromName("word.doc");
		assertNotNull(mimeType);
		mimeType = MimeTypes.getMimeTypeFromName("/thePath/to/the/file.xml");
		assertNotNull(mimeType);
		mimeType = MimeTypes.getMimeTypeFromName("/thePath/to/the/file.dtd");
		assertNotNull(mimeType);
	}
}
