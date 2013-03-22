package ikube.index.parse.word;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class AsposeWordParserTest extends ATest {

	public AsposeWordParserTest() {
		super(AsposeWordParserTest.class);
	}

	@Test
	public void parse() throws Exception {
		// final InputStream inputStream, final OutputStream outputStream
		AsposeWordParser asposeWordParser = new AsposeWordParser();
		File file = FileUtilities.findFileRecursively(new File("."), "docx.docx");
		byte[] bytes = FileUtilities.getContents(file, Integer.MAX_VALUE).toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		asposeWordParser.parse(inputStream, outputStream);
		String text = outputStream.toString();
		assertTrue("Document contains this text : ", text.contains("Determination of the location"));
	}

}
