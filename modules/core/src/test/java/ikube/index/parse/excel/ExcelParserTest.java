package ikube.index.parse.excel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.PerformanceTester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ExcelParserTest extends ATest {

	@Test
	public void parse() throws Exception {
		File file = FileUtilities.findFile(new File("."), new String[] { "xls.xls" });
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		IParser parser = ParserProvider.getParser("application/vnd.ms-excel", bytes);
		InputStream inputStream = new ByteArrayInputStream(bytes);
		for (int i = 0; i < 3; i++) {
			performance(inputStream, parser);
		}
	}

	protected void performance(final InputStream inputStream, final IParser parser) throws Exception {
		PerformanceTester.execute(new PerformanceTester.APerform() {
			@Override
			public void execute() throws Exception {
				inputStream.reset();
				OutputStream parsed = parser.parse(inputStream, new ByteArrayOutputStream());
				assertNotNull(parsed);
				assertTrue(parsed.toString().length() > 0);
				assertTrue(parsed.toString().contains("Michael"));
			}
		}, "Excel parser performance : ", 1000);
	}

}
