package ikube.index.parse.pdf;

import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;

import org.junit.Test;

public class PdfParserTest extends ATest {

	@Test
	public void test() throws Exception {
		PdfParser pdfParser = new PdfParser();
		File file = new File("./modules/core/src/test/resources/data/files/pdf.pdf");
		byte[] bytes = FileUtilities.getContents(file).toByteArray();
		OutputStream parsed = pdfParser.parse(new ByteArrayInputStream(bytes));
		logger.debug("Parsed : " + parsed.toString());
	}

}
