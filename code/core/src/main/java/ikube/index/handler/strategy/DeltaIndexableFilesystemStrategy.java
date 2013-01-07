package ikube.index.handler.strategy;

import ikube.IConstants;
import ikube.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.service.ISearcherService;
import ikube.toolkit.HashUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the delta strategy for the file system handler. Essentially what this class should do is to check to see if the document/file
 * being processed already exists in the current index. If it does, and the time stamp and the length are the same then return a false
 * indicator, meaning that the handler should not add this document to the index.
 * 
 * @author Michael Couck
 * @since 12.12.12
 * @version 01.00
 */
public class DeltaIndexableFilesystemStrategy extends AStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeltaIndexableFilesystemStrategy.class);

	@Autowired
	private ISearcherService searcherService;

	public DeltaIndexableFilesystemStrategy() {
		this(null);
	}

	public DeltaIndexableFilesystemStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preProcess(final Object... parameters) {
		if (parameters == null || parameters.length != 3) {
			LOGGER.warn("Wrong strategy interceptor : " + Arrays.deepToString(parameters));
			return Boolean.TRUE;
		}
		boolean mustProcess = Boolean.TRUE;
		IndexContext<?> indexContext = (IndexContext<?>) parameters[0];
		IndexableFileSystem indexableFileSystem = (IndexableFileSystem) parameters[1];
		File file = (File) parameters[2];
		String indexName = indexContext.getIndexName();
		String[] searchFields = new String[] { indexableFileSystem.getPathFieldName() };
		String[] searchStrings = new String[] { file.getAbsolutePath() };

		String fileId = HashUtilities.hash(file.getAbsolutePath()).toString();
		ArrayList<HashMap<String, String>> results = null;
		results = searcherService.searchMulti(indexName, searchStrings, searchFields, Boolean.FALSE, 0, 10);

		HashMap<String, String> result = isPathSame(fileId, results);
		// LOGGER.info("File id : " + fileId + ", " + results);
		if (result != null) {
			// LOGGER.info("Found : " + result);
			// If the length and the time stamp are different then delete the file and process again
			boolean lengthAndTimestampSame = isLengthAndTimestampSame(indexableFileSystem, file, result);
			// LOGGER.info("Length and timestamp same : " + lengthAndTimestampSame);
			if (lengthAndTimestampSame) {
				mustProcess = Boolean.FALSE;
			} else {
				LOGGER.error("Deleting index entry to replace with latest version : " + fileId + ", " + file.getAbsolutePath());
				Term term = new Term(IConstants.FILE_ID, fileId);
				IndexWriter[] indexWriters = indexContext.getIndexWriters();
				// LOGGER.info("Index context : " + indexContext + ", " + Arrays.deepToString(indexWriters));
				for (final IndexWriter indexWriter : indexWriters) {
					try {
						indexWriter.deleteDocuments(term);
					} catch (CorruptIndexException e) {
						LOGGER.error("Index corrupt, can't really recover from this : ", e);
						throw new RuntimeException(e);
					} catch (IOException e) {
						LOGGER.error("IO exception to the index? Network failure, disk failure? No recovery possible I think : ", e);
						throw new RuntimeException(e);
					} catch (Exception e) {
						LOGGER.error("General exception deleting an out of date document : ", e);
					}
				}
			}
		}
		// LOGGER.error("Continuing with processing : " + mustProcess + ", " + indexContext.getIndexName() + ", " + file.getAbsolutePath());
		return mustProcess;
	}

	private HashMap<String, String> isPathSame(final String fileId, final ArrayList<HashMap<String, String>> results) {
		for (final HashMap<String, String> result : results) {
			String indexFileId = result.get(IConstants.FILE_ID);
			if (indexFileId == null) {
				continue;
			}
			// LOGGER.info("File id : " + indexFileId);
			if (indexFileId.equals(fileId)) {
				return result;
			}
		}
		return null;
	}

	private boolean isLengthAndTimestampSame(final IndexableFileSystem indexableFileSystem, final File file,
			final HashMap<String, String> result) {
		String indexLength = result.get(indexableFileSystem.getLengthFieldName());
		String indexTimestamp = result.get(indexableFileSystem.getLastModifiedFieldName());
		// LOGGER.info("Length : " + indexLength + ", " + file.length() + ", timestamp : " + indexTimestamp + ", " + file.lastModified());
		if (Long.parseLong(indexLength) == file.length() && Long.parseLong(indexTimestamp) == file.lastModified()) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postProcess(final Object... parameters) {
		return nextStrategy != null ? nextStrategy.postProcess(parameters) : true;
	}

}