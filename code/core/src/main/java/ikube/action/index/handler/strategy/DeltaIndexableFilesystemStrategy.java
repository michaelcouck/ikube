package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.HASH;
import org.apache.lucene.document.Document;

import java.io.File;

/**
 * This is the delta strategy for the file system handler. Essentially what this class should do is to check to
 * see if the document/file being processed already exists in the current index. If it does, and the time stamp and
 * the length are the same then return a false indicator, meaning that the handler should not add this document to
 * the index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-12-2012
 */
public class DeltaIndexableFilesystemStrategy extends AStrategy {

    public DeltaIndexableFilesystemStrategy() {
        this(null);
    }

    public DeltaIndexableFilesystemStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(
        final IndexContext indexContext,
        final Indexable indexable,
        final Document document,
        final Object resource)
            throws Exception {
        // Check that the file is changed or doesn't exist, if changed or doesn't exist then process the
        // method, add the resource to the file system file as a reference against the index
        File file = (File) resource;
        String path = file.getAbsolutePath();
        String length = Long.toString(file.length());
        String lastModified = Long.toString(file.lastModified());
        Long identifier = HASH.hash(path, length, lastModified);
        boolean mustProceed = !indexContext.getHashes().remove(identifier);
        // logger.info("Around process delta file strategy : " + mustProceed);
        return mustProceed && super.aroundProcess(indexContext, indexable, document, resource);
    }

}