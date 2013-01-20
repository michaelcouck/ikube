package ikube.interceptor;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.HashUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @since 18.01.12
 * @version 01.00
 */
public class ActionInterceptor implements IActionInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActionInterceptor.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		LOGGER.error("Pre process : ");
		Object[] args = proceedingJoinPoint.getArgs();
		IndexContext<?> indexContext = (IndexContext<?>) args[0];
		if (indexContext.isDelta()) {
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
							LOGGER.info("Added hash for : " + identifier + ", " + length + ", " + lastModified + ", " + path);
						}
					}
				}
			}
			Collections.sort(hashes);
			indexContext.setHashes(hashes);
		}
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		LOGGER.error("Post process : ");
		Object[] args = proceedingJoinPoint.getArgs();
		IndexContext<?> indexContext = (IndexContext<?>) args[0];
		if (indexContext.isDelta()) {
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
								LOGGER.info("Removed old file : " + identifier + ", " + length + ", " + lastModified + ", " + path);
							}
						}
					}
				}
			}
		}
		indexContext.getHashes().clear();
		return Boolean.TRUE;
	}

}