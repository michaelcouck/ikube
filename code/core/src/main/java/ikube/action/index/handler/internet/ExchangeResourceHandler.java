package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.action.index.parse.XMLParser;
import ikube.model.IndexContext;
import ikube.model.IndexableExchange;
import org.apache.lucene.document.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
public class ExchangeResourceHandler extends ResourceHandler<IndexableExchange> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(final IndexContext indexContext, final IndexableExchange indexableExchange, final Document document, final Object resource)
            throws Exception {
        // Parse the content from the email for good measure
        parseContent(indexableExchange);
        // Add the contents field
        IndexManager.addStringField(IConstants.CONTENTS, (String) indexableExchange.getContent(), indexableExchange, document);
        super.addDocument(indexContext, document);
        return document;
    }

    protected void parseContent(final IndexableExchange indexableExchange) {
        try {
            // We assume that the content is text, and a string
            String content = (String) indexableExchange.getContent();
            byte[] buffer = content.getBytes();
            String contentType = "text/html";
            // The first few bytes so we can guess the content type
            byte[] bytes = new byte[Math.min(buffer.length, 1024)];
            System.arraycopy(buffer, 0, bytes, 0, bytes.length);
            IParser parser = ParserProvider.getParser(contentType, bytes);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer, 0, buffer.length);
            OutputStream outputStream = null;
            try {
                outputStream = parser.parse(byteArrayInputStream, new ByteArrayOutputStream());
            } catch (final Exception e) {
                // If this is an XML exception then try the HTML parser
                if (XMLParser.class.isAssignableFrom(parser.getClass())) {
                    contentType = "text/html";
                    parser = ParserProvider.getParser(contentType, bytes);
                    outputStream = parser.parse(byteArrayInputStream, new ByteArrayOutputStream());
                } else {
                    String message = "Exception parsing content from email : " + indexableExchange.getContent();
                    logger.error(message, e);
                }
            }
            if (outputStream != null) {
                String parsedContent = outputStream.toString();
                indexableExchange.setContent(parsedContent);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
