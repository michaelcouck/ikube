package ikube.index.parse.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class XmlParserTest extends ATest {

	public XmlParserTest() {
		super(XmlParserTest.class);
	}

	@Test
	public void parse() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), new String[] { "xml.xml" });
		byte[] bytes = FileUtilities.getContents(file, Integer.MAX_VALUE).toByteArray();
		IParser parser = ParserProvider.getParser("text/xml", bytes);
		OutputStream parsed = parser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream());
		assertNotNull(parsed);
		assertTrue(parsed.toString().length() > 0);
		assertTrue(parsed.toString().contains("ikube"));
		assertTrue(parsed.toString().contains("modelVersion"));
	}

}
