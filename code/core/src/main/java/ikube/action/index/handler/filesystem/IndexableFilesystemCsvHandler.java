package ikube.action.index.handler.filesystem;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableColumn;
import ikube.model.IndexableFileSystemCsv;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * This handler is a custom handler for the CSV files. Rather than inserting the data into the database,
 * meaning creating tables and the like, and the painful process of importing the data using some tool, and
 * then the file is updated and automation is required, this handler will just read the 'structured' files
 * line by line and index the data as if it were a database table.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2011
 */
public class IndexableFilesystemCsvHandler extends IndexableHandler<IndexableFileSystemCsv> {

    @Autowired
    private RowResourceHandler rowResourceHandler;

    @Override
    public ForkJoinTask<?> handleIndexableForked(final IndexContext indexContext,
                                                 final IndexableFileSystemCsv indexableFileSystem) throws Exception {
        IResourceProvider<List<IndexableColumn>> resourceProvider = new FileSystemCsvResourceProvider(indexableFileSystem);
        return getRecursiveAction(indexContext, indexableFileSystem, resourceProvider);
    }

    @Override
    protected List<?> handleResource(final IndexContext indexContext, final IndexableFileSystemCsv indexableFileSystemCsv,
                                     final Object resource) {
        try {
            rowResourceHandler.handleResource(indexContext, indexableFileSystemCsv, new Document(), resource);
        } catch (final Exception e) {
            handleException(indexableFileSystemCsv, e, "Exception handling csv file : " + resource);
        }
        // We return the used rows so the list can be recycled rather than cloning the entities again
        return Collections.singletonList(resource);
    }

}