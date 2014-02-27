package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.parse.IParser;
import ikube.action.index.parse.ParserProvider;
import ikube.action.index.parse.XMLParser;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.UriUtilities;
import net.htmlparser.jericho.*;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.text.html.HTML;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
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
        IResourceProvider resourceProvider = new InternetResourceProvider(indexableInternet, dataBase);
        return getRecursiveAction(indexContext, indexableInternet, resourceProvider);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<Url> handleResource(
            final IndexContext<?> indexContext,
            final IndexableInternet indexableInternet,
            final Object resource) {
        Url url = (Url) resource;
        try {
            // Parse the content from the url
            if (url.getRawContent() != null) {
                parseContent(url);
                internetResourceHandler.handleResource(indexContext, indexableInternet, new Document(), url);
                return extractLinksFromContent(indexableInternet, url);
            }
            return Collections.EMPTY_LIST;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            url.setRawContent(null);
            url.setParsedContent(null);
        }
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
                    String message = "Exception parsing content from url : " + url;
                    logger.error(message, e);
                    handleException(null, e, message);
                }
            }
            if (outputStream != null) {
                String parsedContent = outputStream.toString();
                logger.debug("Parsed content length : " + parsedContent.length());
                url.setParsedContent(parsedContent);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected List<Url> extractLinksFromContent(
            final IndexableInternet indexableInternet,
            final Url url)
            throws IOException {
        if (url.getRawContent() == null) {
            return Collections.EMPTY_LIST;
        }
        List<Url> urls = new ArrayList<>();
        InputStream inputStream = new ByteArrayInputStream(url.getRawContent());
        Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
        Source source = new Source(reader);
        List<Tag> tags = source.getAllTags();
        String baseUrlStripped = indexableInternet.getBaseUrl();
        for (final Tag tag : tags) {
            if (tag.getName().equals(HTMLElementName.A) && StartTag.class.isAssignableFrom(tag.getClass())) {
                Attribute attribute = ((StartTag) tag).getAttributes().get(HTML.Attribute.HREF.toString());
                if (attribute == null) {
                    continue;
                }
                try {
                    String link = attribute.getValue();
                    if (link == null) {
                        continue;
                    }
                    if (UriUtilities.isExcluded(link.trim().toLowerCase())) {
                        continue;
                    }
                    String resolvedLink = UriUtilities.resolve(indexableInternet.getUri(), link);
                    String replacement = resolvedLink.contains("?") ? "?" : "";
                    String strippedSessionLink = UriUtilities.stripJSessionId(resolvedLink, replacement);
                    String strippedAnchorLink = UriUtilities.stripAnchor(strippedSessionLink, "");
                    if (!UriUtilities.isInternetProtocol(strippedAnchorLink)) {
                        continue;
                    }
                    if (!strippedAnchorLink.startsWith(baseUrlStripped)) {
                        continue;
                    }
                    if (indexableInternet.isExcluded(strippedAnchorLink)) {
                        continue;
                    }
                    logger.debug("Link : " + link);
                    Url newUrl = getUrl(indexableInternet.getName(), strippedAnchorLink);
                    urls.add(newUrl);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return urls;
    }

    private Url getUrl(final String name, final String stringUrl) {
        Url url = new Url();
        url.setName(name);
        url.setUrl(stringUrl);
        url.setIndexed(Boolean.FALSE);
        return url;
    }

}