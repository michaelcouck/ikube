package ikube.index.parse.excel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.index.parse.BaseParserTest;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;

import org.junit.Test;

public class ExcelParserTest extends BaseParserTest {

	@Test
	public void parse() throws Exception {
		File file = FileUtilities.findFile(new File("."), new String[] { "xls.xls" }, new ArrayList<File>());
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		IParser parser = ParserProvider.getParser("application/vnd.ms-excel", bytes);
		OutputStream parsed = parser.parse(new ByteArrayInputStream(bytes));
		assertNotNull(parsed);
		assertTrue(parsed.toString().length() > 0);
		assertTrue(parsed.toString().contains("Michael"));
		logger.debug("Parsed : " + parsed.toString());
	}

}
