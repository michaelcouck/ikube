package ikube.interceptor;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;
import ikube.model.Server;
import ikube.toolkit.HashUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michael Couck
 * @since 18.01.12
 * @version 01.00
 */
public class ActionInterceptor implements IActionInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActionInterceptor.class);

	@Autowired
	private IClusterManager clusterManager;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preProcess(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		LOGGER.error("Pre process : ");

		Object[] args = proceedingJoinPoint.getArgs();
		IndexContext<?> indexContext = (IndexContext<?>) args[0];

		openIndexWriters(indexContext);

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
		closeIndexWriters(indexContext);
		return Boolean.TRUE;
	}

	private void closeIndexWriters(final IndexContext<?> indexContext) throws Exception {
		IndexManager.closeIndexWriters(indexContext);
		indexContext.setIndexWriters();
	}

	private void openIndexWriters(final IndexContext<?> indexContext) throws Exception {
		if (indexContext.isDelta()) {
			LOGGER.info("Pre process action : " + this.getClass());
			List<Indexable<?>> indexables = indexContext.getIndexables();
			LOGGER.info("Index delta : " + indexables.size());
			// Start the indexing for this server
			IndexWriter[] indexWriters = IndexManager.openIndexWriterDelta(indexContext);
			indexContext.setIndexWriters(indexWriters);
			LOGGER.info("Index delta : " + Arrays.deepToString(indexWriters) + ", " + indexContext);
		} else {
			Server server = clusterManager.getServer();
			long startTime = System.currentTimeMillis();
			// Start the indexing for this server
			IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, startTime, server.getAddress());
			IndexWriter[] indexWriters = new IndexWriter[] { indexWriter };
			indexContext.setIndexWriters(indexWriters);
		}
	}

}