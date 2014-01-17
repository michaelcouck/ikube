package ikube.action;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.search.SearcherService;
import ikube.toolkit.ThreadUtilities;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This action is called to open the searcher on potentially new indexes. The close operation should run just before this action. In that case the searcher in
 * the index context will be null which is an indication that the searcher should be opened on the new index. The trigger then for this action to complete
 * successfully is the searcher in the context being null.
 * <p/>
 * This action will find the latest index directory based on the folder name which is a long(the time the index was started), find all the server directories in
 * the directory and open a searchable on each of the 'server indexes'. A multi searcher will be opened on the SEARCHABLES and this will be set in the index
 * context. An even will be fired to alert all interested parties that there is a new searcher and they can perform whatever logic the need to, like the
 * {@link SearcherService} which will then open the single and multi search objects on the new multi searcher.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 31.10.10
 */
public class Open extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) {
		return openOnFile(indexContext);
	}

	boolean openOnFile(final IndexContext<?> indexContext) {
		final IndexSearcher oldIndexSearcher = indexContext.getMultiSearcher();
		// First open the new searchables
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory == null) {
			logger.info("No indexes : " + indexDirectoryPath);
			return Boolean.FALSE;
		}
		List<IndexReader> indexReaders = new ArrayList<>();
		File[] serverIndexDirectories = latestIndexDirectory.listFiles();
		if (serverIndexDirectories != null) {
			logger.info("Opening searcher on : " + indexContext.getName());
			for (final File serverIndexDirectory : serverIndexDirectories) {
				logger.info("Index directory : " + serverIndexDirectory);
				Directory directory = null;
				IndexReader reader = null;
                boolean open = Boolean.FALSE;
				try {
					directory = NIOFSDirectory.open(serverIndexDirectory);
					if (!DirectoryReader.indexExists(directory)) {
						directory.close();
						logger.info("Not opening index : " + serverIndexDirectory);
						continue;
					}
					if (IndexWriter.isLocked(directory)) {
						logger.info("Opening reader on locked directory, indexing perhaps?");
					}
					reader = DirectoryReader.open(directory);
					indexReaders.add(reader);
                    open = Boolean.TRUE;
					logger.info("Opened index on : " + serverIndexDirectory);
				} catch (final Exception e) {
					logger.error("Exception opening directory : " + serverIndexDirectory, e);
				} finally {
                    if (!open) {
                        indexReaders.remove(reader);
                        close(directory, reader);
                    }
                }
			}
		}

        if (indexReaders.size() > 0) {
            IndexReader indexReader = new MultiReader(indexReaders.toArray(new IndexReader[indexReaders.size()]), Boolean.TRUE);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexContext.setMultiSearcher(indexSearcher);
        }

		// Make sure that the old searchables are closed,
		// but give them some time for the actions on them to finish
		if (oldIndexSearcher != null && oldIndexSearcher.getIndexReader() != null) {
			ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
				public void run() {
					try {
						ThreadUtilities.sleep(300000);
						oldIndexSearcher.getIndexReader().close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		}

		return Boolean.TRUE;
	}

}