package ikube.action;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.HashUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * @author Michael Couck
 * @since 26.12.12
 * @version 01.00
 */
public class IndexDelta extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preExecute(final IndexContext<?> indexContext) throws Exception {
		logger.info("Pre process action : " + this.getClass() + ", " + indexContext.getName());
		IndexWriter[] indexWriters = IndexManager.openIndexWriterDelta(indexContext);
		indexContext.setIndexWriters(indexWriters);

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

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalExecute(final IndexContext<?> indexContext) throws Exception {
		logger.info("Execute internal : " + this.getClass() + ", " + indexContext.getName());
		List<Indexable<?>> indexables = indexContext.getIndexables();
		Iterator<Indexable<?>> iterator = indexables.iterator();
		executeIndexables(indexContext, iterator);
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postExecute(final IndexContext<?> indexContext) throws Exception {
		logger.info("Post process action : " + this.getClass() + ", " + indexContext.getName());
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
							// Term term = new Term(IConstants.FILE_ID, fileId);
							// indexWriter.deleteDocuments(term);

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
		IndexManager.closeIndexWriters(indexContext);
		indexContext.setIndexWriters();
		return Boolean.TRUE;
	}

}