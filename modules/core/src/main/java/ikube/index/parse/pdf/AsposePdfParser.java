package ikube.index.parse.pdf;

//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;

import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

// import com.aspose.pdf.kit.PdfExtractor;

/**
 * This is the implementation of the Aspose parser for PDF extraction. An alternative to the PDFBox parser which seems to have some memory
 * leaks and not suited to large amounts of data processing.
 *
 * @author Michael Couck
 * @since 17.04.08
 * @version 01.00
 */
public class AsposePdfParser implements IParser {

	/** Logger for the parser class. */
	private Logger LOGGER = Logger.getLogger(AsposePdfParser.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OutputStream parse(InputStream inputStream) {
		try {
			// // Get the bytes to convert to text
			// ByteArrayInputStream inputStream = new ByteArrayInputStream(resource.getBytes());
			// // Instantiate PdfExtractor object
			// PdfExtractor extractor = new PdfExtractor();
			// // Bind the input PDF bytes to extractor
			// extractor.bindPdf(inputStream); // path + "Text.pdf"
			// // Do the extraction
			// extractor.extractText();
			// // Write the extracted text to the output stream
			// ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			// extractor.getText(outputStream); // path + "text.txt"
			// // Set the parsed bytes in the resource for futher processing
			// resource.setBytes(outputStream.toByteArray());
		} catch (Exception t) {
			LOGGER.error("Exception parsing PDF doc with the Aspose parser", t);
		}
		return null;
	}
}
