package ikube.index.parse.pp;

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
public class AsposePowerPointParserTest extends ATest {

	public AsposePowerPointParserTest() {
		super(AsposePowerPointParserTest.class);
	}

	@Test
	public void parse() throws Exception {
		// final InputStream inputStream, final OutputStream outputStream
		AsposePowerPointParser asposePowerPointParser = new AsposePowerPointParser();
		File file = FileUtilities.findFileRecursively(new File("."), "pptx.pptx");
		byte[] bytes = FileUtilities.getContents(file, Integer.MAX_VALUE).toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		asposePowerPointParser.parse(inputStream, outputStream);
		String text = outputStream.toString();
		String fragment = "Complete development in mdot and mobile";
		assertTrue("Document must contain this text : " + fragment, text.contains(fragment));
	}

}
