package ikube.index.handler.strategy;

import ikube.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.service.ISearcherService;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
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
	 * 
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	@Override
	public boolean preProcess(final Object... parameters) throws Exception {
		// Iterate over the index documents and build a file with all the resources
		// in it, including the time stamp and the length and sort the file lexicographically
		IndexContext<?> indexContext = (IndexContext<?>) parameters[0];
		IndexableFileSystem indexableFileSystem = (IndexableFileSystem) parameters[1];
		for (final IndexWriter indexWriter : indexContext.getIndexWriters()) {
			IndexReader indexReader = IndexReader.open(indexWriter.getDirectory());
			for (int i = 0; i < indexReader.numDocs(); i++) {
				Document document = indexReader.document(i);
				String path = document.get(indexableFileSystem.getPathFieldName());
				String length = document.get(indexableFileSystem.getLengthFieldName());
				String lastModified = document.get(indexableFileSystem.getLastModifiedFieldName());
			}
		}
		return super.preProcess(parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final Object... parameters) throws Exception {
		boolean mustProceed = Boolean.FALSE;
		// Check that the file is changed of doesn't exist, if changed or doesn't exist then process the
		// method, add the resource to the file system file as a reference against the index
		IndexContext<?> indexContext = (IndexContext<?>) parameters[0];
		IndexableFileSystem indexableFileSystem = (IndexableFileSystem) parameters[1];
		java.io.File file = (java.io.File) parameters[2];
		return mustProceed && super.preProcess(parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postProcess(final Object... parameters) throws Exception {
		// Check the output file from iterating the file system and for all the entries that are in the file
		// but are not in the file created from the index delete the resources from the index
		return super.preProcess(parameters);
	}

}