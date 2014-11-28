package ikube.action.index.parse;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.FILE;

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
public class XmlParserTest extends AbstractTest {

	@Test
	public void parse() throws Exception {
		File file = FILE.findFileRecursively(new File("."), new String[]{"xml.xml"});
		byte[] bytes = FILE.getContents(file, Integer.MAX_VALUE).toByteArray();
		IParser parser = ParserProvider.getParser("text/xml", bytes);
		OutputStream parsed = parser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream());
		assertNotNull(parsed);
		assertTrue(parsed.toString().length() > 0);
		assertTrue(parsed.toString().contains("ikube"));
		assertTrue(parsed.toString().contains("modelVersion"));
	}

	@Test
	public void noDtd() throws Exception {
		File file = FILE.findFileRecursively(new File("."), new String[]{"69-language-selector-zh-cn.conf"});
		byte[] bytes = FILE.getContents(file, Integer.MAX_VALUE).toByteArray();
		IParser parser = ParserProvider.getParser("text/xml", bytes);
		OutputStream parsed = parser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream());
		assertNotNull(parsed);
		assertTrue(parsed.toString().length() > 0);
		assertTrue(parsed.toString().contains("DejaVu Serif"));
		assertTrue(parsed.toString().contains("prepend"));
	}

}
