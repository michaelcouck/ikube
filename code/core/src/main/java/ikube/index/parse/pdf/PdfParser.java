package ikube.index.parse.pdf;

import ikube.index.parse.IParser;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * Parser for the PDF format.
 * 
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00
 */
public class PdfParser implements IParser {

	/** Logger for the parser class. */
	private static final Logger LOGGER = Logger.getLogger(PdfParser.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		return parsePDFBox(inputStream, outputStream);
	}

	protected OutputStream parsePDFBox(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		// In memory representation of pdf file
		PDDocument pdfDocument = null;
		try {
			PDFParser parser = new PDFParser(inputStream);
			parser.parse();
			pdfDocument = parser.getPDDocument();
			if (pdfDocument.isEncrypted()) {
				// Just try using the default password and move on
				// DecryptDocument decr = new DecryptDocument(pdf);
				// decr.decryptDocument("");
			}
			// collect text
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(pdfDocument);
			outputStream.write(text.getBytes());
			// collect title
			PDDocumentInformation info = pdfDocument.getDocumentInformation();
			String title = info.getTitle();
			if (title != null) {
				outputStream.write(title.getBytes());
			}
			// more useful info, currently not used. please keep them for future use.
			// pdf.getPageCount();info.getAuthor();info.getSubject();info.getKeywords();
			// info.getCreator();info.getProducer();info.getTrapped();formatDate(info.getCreationDate())
			// formatDate(info.getModificationDate());
		} finally {
			try {
				pdfDocument.getDocument().close();
				pdfDocument.close();
			} catch (Exception t) {
				LOGGER.error("Exception thrown closing pdf : " + inputStream, t);
			}
		}
		return outputStream;
	}

}