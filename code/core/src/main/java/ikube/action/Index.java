package ikube.action;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.IIndexableHandler;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.model.Server;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * This class executes the handlers on the indexables, effectively creating the index. Each indexable has a handler that is implemented to handle it. Each
 * handler will return a list of threads that will do the indexing. The caller(in this case, this class) must then wait for the threads to finish.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Index extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preExecute(final IndexContext<?> indexContext) throws Exception {
		logger.info("Pre process action : " + this.getClass() + ", " + indexContext.getName());
		long startTime = System.currentTimeMillis();

		IndexWriter[] indexWriters = null;
		if (!indexContext.isDelta()) {
			Server server = clusterManager.getServer();
			// Start the indexing for this server
			IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, startTime, server.getAddress());
			indexWriters = new IndexWriter[] { indexWriter };
		} else {
			indexWriters = IndexManager.openIndexWriterDelta(indexContext);
			List<Long> hashes = new ArrayList<Long>();
			for (final Indexable<?> indexable : indexContext.getChildren()) {
				if (IndexableFileSystem.class.isAssignableFrom(indexable.getClass())) {
					IndexableFileSystem indexableFileSystem = (IndexableFileSystem) indexable;
					for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
						if (!IndexReader.indexExists(indexWriter.getDirectory())) {
							continue;
						}
						IndexReader indexReader = IndexReader.open(indexWriter.getDirectory());
						for (int i = 0; i < indexReader.numDocs(); i++) {
							Document document = indexReader.document(i);
							String path = document.get(indexableFileSystem.getPathFieldName());
							String length = document.get(indexableFileSystem.getLengthFieldName());
							String lastModified = document.get(indexableFileSystem.getLastModifiedFieldName());
							long identifier = HashUtilities.hash(path, length, lastModified);
							hashes.add(identifier);
							logger.info("Added hash for : " + identifier + ", " + length + ", " + lastModified + ", " + path);
						}
					}
				}
			}
			Collections.sort(hashes);
			indexContext.setHashes(hashes);
		}
		indexContext.setIndexWriters(indexWriters);
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean internalExecute(final IndexContext<?> indexContext) throws Exception {
		List<Indexable<?>> indexables = indexContext.getChildren();
		Iterator<Indexable<?>> iterator = new ArrayList(indexables).iterator();
		// This is the current indexable name
		String indexableName = null;
		while (iterator.hasNext()) {
			Indexable<?> indexable = iterator.next();
			// Update the action with the new indexable
			Server server = clusterManager.getServer();
			ikube.model.Action action = getAction(server, indexContext, indexableName);
			// Now we set the current indexable name in the action
			indexableName = indexable.getName();
			action.setIndexableName(indexableName);
			// Pop the actions back in the grid
			clusterManager.put(server.getAddress(), server);
			// Get the right handler for this indexable
			IIndexableHandler<Indexable<?>> handler = getHandler(indexable);
			logger.info("Indexable : " + indexable.getName());
			// Execute the handler and wait for the threads to finish
			List<Future<?>> futures = handler.handleIndexable(indexContext, indexable);
			ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
		}
		return Boolean.TRUE;
	}

	ikube.model.Action getAction(final Server server, final IndexContext<?> indexContext, final String indexableName) {
		for (final ikube.model.Action action : server.getActions()) {
			if (action.getActionName().equals(this.getClass().getSimpleName())) {
				if (!action.getIndexName().equals(indexContext.getName())) {
					continue;
				}
				if (StringUtils.isEmpty(indexableName)) {
					// We return the first action in the case the indexable name has not been set
					return action;
				} else {
					// Else we look for the action that has the current indexable name
					if (indexableName.equals(action.getIndexableName())) {
						return action;
					}
				}
			}
		}
		throw new RuntimeException("Action not found for class : " + this.getClass().getSimpleName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postExecute(final IndexContext<?> indexContext) throws Exception {
		logger.info("Post process action : " + this.getClass() + ", " + indexContext.getName());
		if (indexContext.isDelta()) {
			List<Long> hashes = indexContext.getHashes();
			for (final Indexable<?> indexable : indexContext.getChildren()) {
				if (IndexableFileSystem.class.isAssignableFrom(indexable.getClass())) {
					IndexableFileSystem indexableFileSystem = (IndexableFileSystem) indexable;
					for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
						if (!IndexReader.indexExists(indexWriter.getDirectory())) {
							continue;
						}
						IndexReader indexReader = IndexReader.open(indexWriter.getDirectory());
						for (int i = 0; i < indexReader.numDocs(); i++) {
							Document document = indexReader.document(i);
							String path = document.get(indexableFileSystem.getPathFieldName());
							String length = document.get(indexableFileSystem.getLengthFieldName());
							String lastModified = document.get(indexableFileSystem.getLastModifiedFieldName());
							long identifier = HashUtilities.hash(path, length, lastModified);
							int index = Collections.binarySearch(hashes, identifier);
							if (index >= 0) {
								logger.info("Num docs : " + indexWriter.numDocs());
								String fileId = document.get(IConstants.FILE_ID);
								BooleanQuery booleanQuery = new BooleanQuery();
								Query termQuery = new TermQuery(new Term(IConstants.FILE_ID, fileId));
								booleanQuery.add(termQuery, BooleanClause.Occur.MUST);
								indexWriter.deleteDocuments(booleanQuery);
								logger.info("Removed old file : " + identifier + ", " + length + ", " + lastModified + ", " + path + ", "
										+ indexWriter.numDocs());
							}
						}
						indexReader.close();
					}
				}
			}
			hashes.clear();
		}
		IndexManager.closeIndexWriters(indexContext);
		indexContext.setIndexWriters();
		return Boolean.TRUE;
	}

}