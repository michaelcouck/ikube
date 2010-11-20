package ikube.index.parse.mime;

import static org.junit.Assert.assertNotNull;
import ikube.ATest;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 10.02.10
 * @version 01.00
 */
public class MimeTypesTest extends ATest {

	@Test
	public void getMimeTypesFromName() {
		MimeType mimeType = MimeTypes.getMimeTypeFromName("text/html");
		assertNotNull(mimeType);
		logger.warn("Mime type : text/html : " + mimeType.getName() + ", " + mimeType.getPrimaryType() + ", " + mimeType.getSubType());
		mimeType = MimeTypes.getMimeTypeFromName("index.html");
		assertNotNull(mimeType);
		logger.warn("Mime type : index.html : " + mimeType.getName() + ", " + mimeType.getPrimaryType() + ", " + mimeType.getSubType());
		mimeType = MimeTypes.getMimeTypeFromName("word.doc");
		assertNotNull(mimeType);
		logger.warn("Mime type : word.doc : " + mimeType.getName() + ", " + mimeType.getPrimaryType() + ", " + mimeType.getSubType());
		mimeType = MimeTypes.getMimeTypeFromName("/thePath/to/the/file.xml");
		assertNotNull(mimeType);
		logger.warn("Mime type : file.xml : " + mimeType.getName() + ", " + mimeType.getPrimaryType() + ", " + mimeType.getSubType());
	}
}
