package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.parse.mime.MimeType;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

/**
 * This strategy will check for the type of content based on a magic number and
 * potentially the identifier of the record and add it to the document.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-11-2013
 */
public final class ContentTypeStrategy extends AStrategy {

    public ContentTypeStrategy() {
        this(null);
    }

    public ContentTypeStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception
     */
    @Override
    public boolean preProcess(
        final IndexContext indexContext,
        final Indexable indexable,
        final Document document,
        final Object resource)
            throws Exception {
        MimeType mimeType = null;
        // Check the name of the resource, i.e. index.html if it iexists
        if (resource != null) {
            String name = FilenameUtils.getName(resource.toString());
            if (!StringUtils.isEmpty(name) && name.indexOf('.') > -1) {
                mimeType = MimeTypes.getMimeTypeFromName(name);
            }
        }
        if (mimeType == null) {
            // Guess the type from the content
            Object rawContent = getRawContent(indexable);
            if (rawContent != null) {
                if (rawContent.getClass().isAssignableFrom(byte[].class)) {
                    mimeType = MimeTypes.getMimeType((byte[]) rawContent);
                } else {
                    mimeType = MimeTypes.getMimeType(rawContent.toString().getBytes());
                }
            }
        }
        if (mimeType != null) {
            IndexManager.addStringField(IConstants.MIME_TYPE, mimeType.getName(), indexable, document);
        }
        return super.preProcess(indexContext, indexable, document, resource);
    }

    final Object getRawContent(final Indexable indexable) {
        Object rawContent = indexable.getRawContent();
        if (rawContent != null) {
            return rawContent;
        } else {
            if (indexable.getChildren() != null) {
                for (final Indexable child : indexable.getChildren()) {
                    rawContent = getRawContent(child);
                    if (rawContent != null) {
                        return rawContent;
                    }
                }
            }
        }
        // No content anywhere? Strange...
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
    }

}