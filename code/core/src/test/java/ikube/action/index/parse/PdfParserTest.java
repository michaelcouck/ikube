package ikube.action.index.parse;

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
public class PdfParserTest extends AbstractTest {

	@Test
	public void parse() throws Exception {
		PdfParser pdfParser = new PdfParser();
		File file = FILE.findFileRecursively(new File("."), new String[]{"pdf.pdf"});
		byte[] bytes = FILE.getContents(file, Integer.MAX_VALUE).toByteArray();
		OutputStream parsed = pdfParser.parse(new ByteArrayInputStream(bytes), new ByteArrayOutputStream());
		assertTrue(parsed.toString().contains("Application form for affiliation"));
	}

}
