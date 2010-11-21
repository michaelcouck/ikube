package ikube.index.parse.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;

import org.junit.Test;

public class XmlParserTest extends ATest {

	@Test
	public void parse() throws Exception {
		File file = FileUtilities.findFile(new File("."), new String[] { "xml.xml" });
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		IParser parser = ParserProvider.getParser("text/xml", bytes);
		OutputStream parsed = parser.parse(new ByteArrayInputStream(bytes));
		assertNotNull(parsed);
		assertTrue(parsed.toString().length() > 0);
		assertTrue(parsed.toString().contains("Hello"));
	}

}
