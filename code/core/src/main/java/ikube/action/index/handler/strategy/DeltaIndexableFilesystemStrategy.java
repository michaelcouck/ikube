package ikube.action.index.handler.strategy;

import ikube.action.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.HashUtilities;

import java.io.File;
import java.util.Collections;

import org.apache.lucene.document.Document;

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
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document,
			final Object resource) throws Exception {
		boolean mustProceed = Boolean.TRUE;
		// Check that the file is changed of doesn't exist, if changed or doesn't exist then process the
		// method, add the resource to the file system file as a reference against the index
		File file = (File) resource;
		String path = file.getAbsolutePath();
		String length = Long.toString(file.length());
		String lastModified = Long.toString(file.lastModified());
		Long identifier = HashUtilities.hash(path, length, lastModified);
		int index = Collections.binarySearch(indexContext.getHashes(), identifier);
		logger.info("Key for, proceeding with file : " + identifier + ", " + length + ", " + lastModified + ", " + path);
		if (index >= 0) {
			mustProceed = Boolean.FALSE;
			// Remove the key because at the end of processing we will delete
			// all the documents in the index that are still in the hash list
			indexContext.getHashes().remove(index);
		}
		logger.info("Around process delta file strategy : " + mustProceed);
		return mustProceed && super.aroundProcess(indexContext, indexable, document, resource);
	}

}