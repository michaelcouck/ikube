package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.action.index.parse.XMLParser;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

    @Autowired
    private InternetResourceHandler internetResourceHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(
            final IndexContext<?> indexContext,
            final IndexableInternet indexableInternet)
            throws Exception {

        IResourceProvider resourceProvider = new InternetResourceProvider(indexableInternet);
        return getRecursiveAction(indexContext, indexableInternet, resourceProvider);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Url> handleResource(
            final IndexContext<?> indexContext,
            final IndexableInternet indexableInternet,
            final Object resource) {
        try {
            Url url = (Url) resource;
            handle(indexContext, indexableInternet, url);
            return Collections.EMPTY_LIST;
        } catch (final Exception e) {
            handleException(indexableInternet, e, "Exception crawling url : " + resource);
        }
        return null;
    }

    /**
     * This method will do the actions that visit the url, parse the data and add it to the index.
     *
     * @param indexContext the index context for this index
     * @param indexable    the internet base url configuration object
     * @param url          the url that will be indexed in this call
     */
    protected void handle(
            final IndexContext<?> indexContext,
            final IndexableInternet indexable,
            final Url url) {
        try {
            // Parse the content from the url
            String parsedContent = getParsedContent(url, url.getRawContent());
            if (parsedContent != null) {
                url.setHash(HashUtilities.hash(parsedContent));
                // Add the document to the index
                internetResourceHandler.handleResource(indexContext, indexable, new Document(), url);
            }
        } catch (final Exception e) {
            handleException(indexable, e);
        } finally {
            url.setRawContent(null);
            url.setParsedContent(null);
        }
    }

    /**
     * Parses the content from the input stream into a string. The content can be anything, rich text, xml, etc.
     *
     * @param url    the url where the data is
     * @param buffer the output stream of data from the url
     * @return the parsed content
     */
    protected String getParsedContent(
            final Url url,
            final byte[] buffer) {
        try {
            String contentType = url.getContentType(); // URI.create(url.getUrl()).toURL().getFile();
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
                    String message = "Exception parsing content from url : " + url;
                    logger.error(message, e);
                    handleException(null, e, message);
                }
            }
            url.setContentType(contentType);
            if (outputStream != null) {
                url.setParsedContent(outputStream.toString());
                return outputStream.toString();
            }
        } catch (final Exception e) {
            handleException(null, e);
        }
        return null;
    }


}