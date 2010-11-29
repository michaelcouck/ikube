package ikube.index.parse.pdf;

import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

// import com.aspose.pdf.kit.PdfExtractor;

/**
 * Note: This parser is 5 times slower than the PdfBox parser.
 * 
 * This is the implementation of the Aspose parser for PDF extraction. An alternative to the PDFBox parser which seems to have some memory
 * leaks and not suited to large amounts of data processing.
 * 
 * @author Michael Couck
 * @since 17.04.08
 * @version 01.00
 */
public class AsposePdfParser implements IParser {

	/** Logger for the parser class. */
	private Logger logger = Logger.getLogger(AsposePdfParser.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream parse(InputStream inputStream, OutputStream outputStream) {
		try {
			// Instantiate PdfExtractor object
			// PdfExtractor extractor = new PdfExtractor();
			// Bind the input PDF bytes to extractor
			// extractor.bindPdf(inputStream); // path + "Text.pdf"
			// Do the extraction
			// extractor.extractText();
			// Write the extracted text to the output stream
			// extractor.getText(outputStream); // path + "text.txt"
		} catch (Exception t) {
			logger.error("Exception parsing PDF doc with the Aspose parser", t);
		}
		return outputStream;
	}
}
