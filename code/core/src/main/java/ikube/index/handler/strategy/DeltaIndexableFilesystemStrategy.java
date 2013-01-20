package ikube.index.handler.strategy;

import ikube.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.toolkit.HashUtilities;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public boolean aroundProcess(final Object... parameters) throws Exception {
		boolean mustProceed = Boolean.FALSE;
		// Check that the file is changed of doesn't exist, if changed or doesn't exist then process the
		// method, add the resource to the file system file as a reference against the index
		IndexContext<?> indexContext = (IndexContext<?>) parameters[0];
		java.io.File file = (java.io.File) parameters[2];
		String path = file.getAbsolutePath();
		String length = Long.toString(file.length());
		String lastModified = Long.toString(file.lastModified());
		Long identifier = HashUtilities.hash(path, length, lastModified);
		int index = Collections.binarySearch(indexContext.getHashes(), identifier);
		if (index < 0) {
			mustProceed = Boolean.TRUE;
			LOGGER.info("Didn't find key for, proceeding with file : " + identifier + ", " + length + ", " + lastModified + ", " + path);
		} else {
			// Remove the key because at the end of processing we will delete
			// all the documents in the index that are still in the hash list
			indexContext.getHashes().remove(index);
		}
		return mustProceed && super.aroundProcess(parameters);
	}

}