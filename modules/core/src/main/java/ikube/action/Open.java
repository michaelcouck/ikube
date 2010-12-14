package ikube.action;

import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.IndexContext;

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
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Open extends Action {

	@Override
	public Boolean execute(IndexContext indexContext) {
		// We wait for the searcher to be closed before we open another one. The
		// close operation is very fast and the open operation is also so any clients will
		// have to be VERY fast to catch the application without a searcher open.
		// This way we don't have to manage open searchers and keep track of them
		if (indexContext.getMultiSearcher() != null) {
			logger.debug("Index searcher still active, will not open : ");
			return Boolean.FALSE;
		}
		// See if there are any index directories that are new and not included
		// in the directories of the existing searchers
		boolean shouldReopen = shouldReopen(indexContext);
		if (!shouldReopen) {
			logger.debug("Shouldn't open : ");
			return Boolean.FALSE;
		}
		// path => index name => time => ip
		ArrayList<Searchable> searchers = new ArrayList<Searchable>();

		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath());
		File[] contextIndexDirectories = baseIndexDirectory.listFiles();
		if (contextIndexDirectories == null) {
			return Boolean.FALSE;
		}
		for (File contextIndexDirectory : contextIndexDirectories) {
			File[] timeIndexDirectories = contextIndexDirectory.listFiles();
			for (File timeIndexDirectory : timeIndexDirectories) {
				File[] serverIndexDirectories = timeIndexDirectory.listFiles();
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
						logger.debug("Opened searcher on : " + serverIndexDirectory + ", exists : " + exists + ", locked : " + locked);
					} catch (Exception e) {
						logger.error("Exception opening directory : " + contextIndexDirectory, e);
					}
				}
			}
		}

		try {
			if (searchers.size() > 0) {
				Searchable[] searchables = searchers.toArray(new IndexSearcher[searchers.size()]);
				MultiSearcher multiSearcher = new MultiSearcher(searchables);
				indexContext.setMultiSearcher(multiSearcher);
				ListenerManager.fireEvent(indexContext, Event.SEARCHER_OPENED, new Timestamp(System.currentTimeMillis()), Boolean.FALSE);
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			logger.error("Exception opening the multi searcher", e);
		}
		return Boolean.FALSE;
	}

}