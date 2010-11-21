package ikube.index.parse.pdf;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class PdfParserTest extends ATest {

	@Test
	public void test() throws Exception {
		PdfParser pdfParser = new PdfParser();
		File file = FileUtilities.findFile(new File("."), new String[] { "pdf.pdf" });
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		OutputStream parsed = pdfParser.parse(new ByteArrayInputStream(bytes));
		assertTrue(parsed.toString().contains("Application form for affiliation"));
	}

}
