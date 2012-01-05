package ikube.index.handler.filesystem;

import ikube.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Future;

/**
 * This class indexes a file share on the network. It is multi threaded but not cluster load balanced.
 * 
 * This class is optimised for performance, as such it is not very elegant.
 * 
 * @author Cristi Bozga
 * @author Michael Couck
 * @since 29.11.10
 * @version 02.00<br>
 *          Updated this class to persist the files and to be multi threaded to improve the performance.
 */
public class IndexableFilesystemHandler extends IndexableHandler<IndexableFileSystem> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handle(final IndexContext<?> indexContext, final IndexableFileSystem indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		try {
			Stack<File> directories = new Stack<File>();
			directories.push(new File(indexable.getPath()));
			for (int i = 0; i < getThreads(); i++) {
				IndexableFileSystem indexableFileSystem = (IndexableFileSystem) SerializationUtilities.clone(indexable);
				Runnable runnable = new IndexableFilesystemHandlerWorker(this, indexContext, indexableFileSystem, directories);
				Future<?> future = ThreadUtilities.submit(runnable);
				futures.add(future);
			}
		} catch (Exception e) {
			logger.error("Exception starting the file system indexer threads : ", e);
		}
		return futures;
	}

}