package ikube.action.index.parse;

import org.apache.log4j.Logger;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Parser for the PDF format.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-05-2004
 */
public class PdfParser implements IParser {

    /**
     * Logger for the parser class.
     */
    private static final Logger LOGGER = Logger.getLogger(PdfParser.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
        return parsePDFBox(inputStream, outputStream);
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
            // Collect information in the document if exists
            // PDDocumentInformation info = pdfDocument.getDocumentInformation();
            // addInfo(info.getTitle(), outputStream);
            // addInfo(info.getAuthor(), outputStream);
            // addInfo(info.getSubject(), outputStream);
            // addInfo(info.getCreator(), outputStream);
            // addInfo(info.getProducer(), outputStream);
            // addInfo(info.getTrapped(), outputStream);
            // addInfo(info.getCreationDate(), outputStream);
            // addInfo(info.getModificationDate(), outputStream);
            // Collect content
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDocument);
            outputStream.write(text.getBytes());
            // More useful info, currently not used. please keep them for future use: pdf.getPageCount()
        } finally {
            try {
                if (pdfDocument != null) {
                    pdfDocument.getDocument().close();
                    pdfDocument.close();
                }
            } catch (Exception t) {
                LOGGER.error("Exception thrown closing pdf : " + pdfDocument, t);
            }
        }
        return outputStream;
    }

    @SuppressWarnings("UnusedDeclaration")
    void addInfo(final Object info, final OutputStream outputStream) throws IOException {
        if (info != null) {
            outputStream.write(info.toString().getBytes());
            outputStream.write(" ".getBytes());
        }
    }

}