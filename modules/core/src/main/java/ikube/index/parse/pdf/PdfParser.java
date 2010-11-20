package ikube.index.parse.pdf;

import ikube.index.parse.IParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

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
	public final OutputStream parse(InputStream inputStream) throws Exception {
		return parsePDFBox(inputStream);
		// return parseAsprise(bytes);
		// return null;
	}

	protected OutputStream parsePDFBox(InputStream inputStream) throws Exception {
		// In memory representation of pdf file
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PDDocument pdf = null;
		try {
			PDFParser parser = new PDFParser(inputStream);
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
			byteArrayOutputStream.write(text.getBytes());
			// collect title
			PDDocumentInformation info = pdf.getDocumentInformation();
			String title = info.getTitle();
			byteArrayOutputStream.write(title.getBytes());
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
					LOGGER.error("Exception thrown closing pdf : " + inputStream, e);
				} catch (Exception t) {
					LOGGER.error("Exception thrown closing pdf : " + inputStream, t);
				}
		}
		return byteArrayOutputStream;
	}

}