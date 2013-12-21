package ikube.action;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.search.SearcherService;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

/**
 * This action is called to open the searcher on potentially new indexes. The close operation should run just before this action. In that case the searcher in
 * the index context will be null which is an indication that the searcher should be opened on the new index. The trigger then for this action to complete
 * successfully is the searcher in the context being null.
 * 
 * This action will find the latest index directory based on the folder name which is a long(the time the index was started), find all the server directories in
 * the directory and open a searchable on each of the 'server indexes'. A multi searcher will be opened on the SEARCHABLES and this will be set in the index
 * context. An even will be fired to alert all interested parties that there is a new searcher and they can perform whatever logic the need to, like the
 * {@link SearcherService} which will then open the single and multi search objects on the new multi searcher.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class Open extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) {
		return openOnFile(indexContext);
	}

	boolean openOnFile(final IndexContext<?> indexContext) {
		MultiSearcher newMultiSearcher = null;
		// First open the new searchables
		ArrayList<Searchable> searchers = new ArrayList<Searchable>();
		String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
		File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory == null) {
			logger.info("No indexes : " + indexDirectoryPath);
			return Boolean.FALSE;
		}
		File[] serverIndexDirectories = latestIndexDirectory.listFiles();
		for (final File serverIndexDirectory : serverIndexDirectories) {
			logger.info("Index directory : " + serverIndexDirectory);
			Directory directory = null;
			IndexReader reader = null;
			Searchable searcher = null;
			boolean opened = Boolean.FALSE;
			try {
				directory = NIOFSDirectory.open(serverIndexDirectory);
				if (!IndexReader.indexExists(directory)) {
					directory.close();
					logger.info("Not opening index : " + serverIndexDirectory);
					continue;
				}
				if (IndexWriter.isLocked(directory)) {
					logger.info("Opening reader on locked directory, indexing perhaps?");
				}
				reader = IndexReader.open(directory);
				searcher = new IndexSearcher(reader);
				searchers.add(searcher);
				opened = Boolean.TRUE;
				logger.info("Opened index on : " + serverIndexDirectory);
			} catch (Exception e) {
				logger.error("Exception opening directory : " + serverIndexDirectory, e);
			} finally {
				if (!opened) {
					close(directory, reader, searcher);
					boolean removed = searchers.remove(searcher);
					logger.warn("Removed searcher : " + removed + ", " + searcher);
				}
			}
		}
		newMultiSearcher = open(indexContext, searchers);

		final MultiSearcher multiSearcher = indexContext.getMultiSearcher();
		// Make sure that the old searchables are closed,
		// but give them some time for the actions on them to finish
		if (newMultiSearcher != null) {
			ThreadUtilities.submitSystem(new Runnable() {
				public void run() {
					ThreadUtilities.sleep(60000);
					closeSearchables(multiSearcher);
				}
			});
		}
		return newMultiSearcher != null;
	}

	MultiSearcher open(final IndexContext<?> indexContext, final List<Searchable> searchers) {
		MultiSearcher multiSearcher = null;
		try {
			Searchable[] searchables = searchers.toArray(new IndexSearcher[searchers.size()]);
			multiSearcher = new MultiSearcher(searchables);
			indexContext.setMultiSearcher(multiSearcher);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return multiSearcher;
	}

}