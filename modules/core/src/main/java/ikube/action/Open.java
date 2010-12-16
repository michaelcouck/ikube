package ikube.action;

import ikube.listener.ListenerManager;
import ikube.logging.Logging;
import ikube.model.Event;
import ikube.model.IndexContext;
import ikube.service.SearcherWebService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.FSDirectory;

/**
 * This action is called to open the searcher on potentially new indexes. The close operation should run just before this action. In that
 * case the searcher in the index context will be null which is an indication that the searcher should be opened on the new index. The
 * trigger then for this action to complete successfully is the searcher in the context being null.
 * 
 * This action will find the latest index directory based on the folder name which is a long(the time the index was started), find all the
 * server directories in the directory and open a searchable on each of the 'server indexes'. A multi searcher will be opened on the
 * searchables and this will be set in the index context. An even will be fired to alert all interested parties that there is a new searcher
 * and they can perform whatever logic the need to, like the {@link SearcherWebService} which will then open the single and multi search
 * objects on the new multi searcher.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Open extends Action {

	@Override
	public Boolean execute(IndexContext indexContext) {
		// We wait for the searcher to be closed before we open another one. The
		// close operation is very fast and the open operation is also, so any clients will
		// have to be VERY fast to catch the application without a searcher open. This
		// way we don't have to manage open searchers and keep track of them
		if (indexContext.getMultiSearcher() != null) {
			logger.debug("Index searcher still active, will not open : ");
			return Boolean.FALSE;
		}
		ArrayList<Searchable> searchers = new ArrayList<Searchable>();

		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath());
		File[] contextIndexDirectories = baseIndexDirectory.listFiles();
		if (contextIndexDirectories == null) {
			return Boolean.FALSE;
		}
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
		logger.info("Latest index directory : " + latestIndexDirectory);
		File[] serverIndexDirectories = latestIndexDirectory.listFiles();
		for (File serverIndexDirectory : serverIndexDirectories) {
			try {
				FSDirectory directory = FSDirectory.open(serverIndexDirectory);
				boolean exists = IndexReader.indexExists(directory);
				boolean locked = IndexWriter.isLocked(directory);
				if (!exists || locked) {
					// We don't open locked directories. Could be
					// that one configuration is still indexing on this
					// file system, but we still want to open the index
					// on the other new indexes. Of course if the index
					// doesn't exist in the directory for some odd reason
					// then we just ignore it, and the problem will eventually
					// get deleted(at the next full index of course).
					continue;
				}
				IndexReader reader = IndexReader.open(directory, Boolean.TRUE);
				Searchable searcher = new IndexSearcher(reader);
				searchers.add(searcher);
				logger.info(Logging.getString("Opened searcher on : ", serverIndexDirectory, ", exists : ", exists, ", locked : ", locked));
			} catch (Exception e) {
				logger.error("Exception opening directory : " + serverIndexDirectory, e);
			}
		}

		try {
			if (searchers.size() > 0) {
				Searchable[] searchables = searchers.toArray(new IndexSearcher[searchers.size()]);
				MultiSearcher multiSearcher = new MultiSearcher(searchables);
				indexContext.setMultiSearcher(multiSearcher);
				ListenerManager.fireEvent(Event.SEARCHER_OPENED, new Timestamp(System.currentTimeMillis()), indexContext, Boolean.FALSE);
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			logger.error("Exception opening the multi searcher", e);
		}
		return Boolean.FALSE;
	}

}