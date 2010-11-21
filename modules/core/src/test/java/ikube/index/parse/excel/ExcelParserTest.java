package ikube.index.parse.excel;

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

public class ExcelParserTest extends ATest {

	@Test
	public void parse() throws Exception {
		File file = FileUtilities.findFile(new File("."), new String[] { "xls.xls" });
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		IParser parser = ParserProvider.getParser("application/vnd.ms-excel", bytes);
		OutputStream parsed = parser.parse(new ByteArrayInputStream(bytes));
		assertNotNull(parsed);
		assertTrue(parsed.toString().length() > 0);
		assertTrue(parsed.toString().contains("Michael"));
	}

}
