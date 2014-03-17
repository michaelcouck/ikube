package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.action.index.parse.XMLParser;
import ikube.action.index.parse.mime.MimeType;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.apache.lucene.document.Document;

import java.io.*;
import java.net.URI;

/**
 * This class will parse the content from the url, extract a title if possible and
 * persist the parsed content in the Lucene document with the correct field names.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class InternetResourceHandler extends ResourceHandler<IndexableInternet> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Document handleResource(
            final IndexContext<?> indexContext,
            final IndexableInternet indexableInternet,
            final Document document,
            final Object resource)
            throws Exception {
        Url url = (Url) resource;
        parseContent(url);
        String parsedContent = url.getParsedContent();
        if (parsedContent == null) {
            return document;
        }
        url.setHash(HashUtilities.hash(parsedContent));
        if (indexContext.getHashes() != null && !indexContext.getHashes().add(url.getHash())) {
            // Duplicate content, not interesting
            logger.info("Duplicate : " + url.getUrl());
            return document;
        }
        // Add the id field, which is the url in this case
        IndexManager.addStringField(indexableInternet.getIdFieldName(), url.getUrl(), indexableInternet, document);
        // Add the title field
        MimeType mimeType = MimeTypes.getMimeType(url.getContentType(), url.getRawContent());
        if (mimeType != null && mimeType.getSubType().toLowerCase().contains(HTMLElementName.HTML.toLowerCase())) {
            InputStream inputStream = new ByteArrayInputStream(url.getRawContent());
            Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
            Source source = new Source(reader);
            Element titleElement = source.getNextElement(0, HTMLElementName.TITLE);
            if (titleElement != null) {
                String title = titleElement.getContent().toString();
                url.setTitle(title);
                IndexManager.addStringField(indexableInternet.getTitleFieldName(), title, indexableInternet, document);
            }
        } else {
            // Add the url as the title
            IndexManager.addStringField(indexableInternet.getTitleFieldName(), url.getUrl(), indexableInternet, document);
        }
        // Add the contents field
        IndexManager.addStringField(indexableInternet.getContentFieldName(), url.getParsedContent(), indexableInternet, document);
        super.addDocument(indexContext, document);
        return document;
    }

    /**
     * Parses the content from the input stream into a string. The content can be anything, rich text, xml, etc.
     *
     * @param url the url where the data is
     */
    protected void parseContent(final Url url) {
        try {
            byte[] buffer = url.getRawContent();
            logger.debug("Buffer length : " + buffer.length);
            String contentType;
            if (url.getContentType() != null) {
                contentType = url.getContentType();
            } else {
                contentType = URI.create(url.getUrl()).toURL().getFile();
                url.setContentType(contentType);
            }
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
                    String message = "Exception parsing content from url : " + url.getUrl();
                    logger.error(message, e);
                }
            }
            if (outputStream != null) {
                String parsedContent = outputStream.toString();
                logger.debug("Parsed content length : " + parsedContent.length() + ", content type : " + url.getContentType());
                url.setParsedContent(parsedContent);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
