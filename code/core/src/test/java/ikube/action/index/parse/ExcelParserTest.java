package ikube.action.index.parse;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.FILE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ExcelParserTest extends AbstractTest {

	@Test
    @Ignore
	public void parse() throws Exception {
		File file = FILE.findFileRecursively(new File("."), new String[]{"xls.xls"});
		byte[] bytes = FILE.getContents(file, Integer.MAX_VALUE).toByteArray();
		IParser parser = ParserProvider.getParser("application/vnd.ms-excel", bytes);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		OutputStream parsed = parser.parse(inputStream, new ByteArrayOutputStream());
		assertNotNull(parsed);
		assertTrue(parsed.toString().length() > 0);
		assertTrue(parsed.toString().contains("Michael"));
	}

}
