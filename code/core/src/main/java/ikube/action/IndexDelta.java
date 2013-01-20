package ikube.action;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.HashUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

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
		logger.info("Pre process action : " + this.getClass());
		List<Indexable<?>> indexables = indexContext.getIndexables();
		logger.info("Index delta : " + indexables.size());
		// Start the indexing for this server
		IndexWriter[] indexWriters = IndexManager.openIndexWriterDelta(indexContext);
		indexContext.setIndexWriters(indexWriters);
		logger.info("Index delta : " + Arrays.deepToString(indexWriters) + ", " + indexContext);

		List<Long> hashes = new ArrayList<Long>();
		for (final Indexable<?> indexable : indexContext.getChildren()) {
			if (IndexableFileSystem.class.isAssignableFrom(indexable.getClass())) {
				IndexableFileSystem indexableFileSystem = (IndexableFileSystem) indexable;
				for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
					IndexReader indexReader = IndexReader.open(indexWriter.getDirectory());
					for (int i = 0; i < indexReader.numDocs(); i++) {
						Document document = indexReader.document(i);
						// /usr/share/apache-tomcat-7.0.27/bin/ikube/common/languages/english.txt,1234567890,1234567890
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
		List<Long> hashes = indexContext.getHashes();
		for (final Indexable<?> indexable : indexContext.getChildren()) {
			if (IndexableFileSystem.class.isAssignableFrom(indexable.getClass())) {
				IndexableFileSystem indexableFileSystem = (IndexableFileSystem) indexable;
				for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
					IndexReader indexReader = IndexReader.open(indexWriter.getDirectory());
					for (int i = 0; i < indexReader.numDocs(); i++) {
						Document document = indexReader.document(i);
						String path = document.get(indexableFileSystem.getPathFieldName());
						String length = document.get(indexableFileSystem.getLengthFieldName());
						String lastModified = document.get(indexableFileSystem.getLastModifiedFieldName());
						long identifier = HashUtilities.hash(path, length, lastModified);
						int index = Collections.binarySearch(hashes, identifier);
						if (index >= 0) {
							String fileId = document.get(IConstants.FILE_ID);
							Term term = new Term(IConstants.FILE_ID, fileId);
							indexWriter.deleteDocuments(term);
							logger.info("Removed old file : " + identifier + ", " + length + ", " + lastModified + ", " + path);
						}
					}
				}
			}
		}
		indexContext.getHashes().clear();
		IndexManager.closeIndexWriters(indexContext);
		indexContext.setIndexWriters();
		return Boolean.TRUE;
	}

}