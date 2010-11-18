package ikube.index.parse.pdf;

import ikube.index.parse.IParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.util.PDFTextStripper;

// import com.asprise.util.pdf.PDFReader;

/**
 * Parser for the PDF format.
 *
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00
 */
public class PdfParser implements IParser {

	/** Logger for the parser class. */
	private Logger LOGGER = Logger.getLogger(PdfParser.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String parse(String string) throws Exception {
		return parsePDFBox(string.getBytes());
		// return parseAsprise(bytes);
	}

	// protected String parseAsprise(byte[] bytes) throws Exception {
	// StringBuilder content = new StringBuilder();
	// ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	// PDFReader reader = new PDFReader(bis);
	// try {
	// reader.open();
	// int pages = reader.getNumberOfPages();
	// for (int i = 0; i < pages; i++) {
	// String text = reader.extractTextFromPage(i);
	// content.append(text);
	// content.append(" ");
	// LOGGER.debug("Page " + i + ": " + text);
	// }
	// } finally {
	// try {
	// if (reader != null) {
	// reader.close();
	// }
	// } catch (IOException e) {
	// LOGGER.error("Exception closing the PDF reader", e);
	// }
	// }
	// return content.toString();
	// }

	protected String parsePDFBox(byte[] bytes) throws Exception {
		// In memory representation of pdf file
		StringBuilder content = new StringBuilder();
		PDDocument pdf = null;
		try {
			PDFParser parser = new PDFParser(new ByteArrayInputStream(bytes));
			parser.parse();
			pdf = parser.getPDDocument();
			if (pdf.isEncrypted()) {
				// Just try using the default password and move on
				// DecryptDocument decr = new DecryptDocument(pdf);
				// decr.decryptDocument("");
			}
			// collect text
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(pdf);
			content.append(text);
			// collect title
			PDDocumentInformation info = pdf.getDocumentInformation();
			String title = info.getTitle();
			content.append(title);
			// more useful info, currently not used. please keep them for future use.
			// pdf.getPageCount();info.getAuthor();info.getSubject();info.getKeywords();
			// info.getCreator();info.getProducer();info.getTrapped();formatDate(info.getCreationDate())
			// formatDate(info.getModificationDate());
		} finally {
			if (pdf != null)
				try {
					pdf.getDocument().close();
					pdf.close();
				} catch (IOException e) {
					LOGGER.error("Exception thrown closing pdf " + new String(bytes), e);
				} catch (Exception t) {
					LOGGER.error("Exception thrown closing pdf " + new String(bytes), t);
				}
		}
		return content.toString();
	}
}