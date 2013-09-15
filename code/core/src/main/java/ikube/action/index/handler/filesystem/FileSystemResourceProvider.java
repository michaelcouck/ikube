package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will provide files to the {@link IndexableFileSystemHandler}. It simply walks the file system from the starting point that is specified, ignoring
 * the files and paths that were specified as excluded, adds the files to the stack and makes them available to the handler. The walk of the file system will
 * pause if the size of the stack is too large, i.e. if the consumers cannot keep pace with the file system walking.
 * 
 * @author Michael Couck
 * @since 25.03.13
 * @version 01.00
 */
class FileSystemResourceProvider implements IResourceProvider<File> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemResourceProvider.class);

	private int included = 0;
	private int excluded = 0;
	private boolean finished = Boolean.FALSE;
	private final Stack<File> files = new Stack<File>();

	/**
	 * The constructor takes the configuration for the walk of the file system, and start a thread that will do the actual walk, so the caller does not have to
	 * wait for the walk to finish before starting the consumption of the files.
	 * 
	 * @param indexableFileSystem the configuration of the file system walk
	 * @param pattern the pattern to exclude from the files accumulated
	 * @throws IOException
	 */
	FileSystemResourceProvider(final IndexableFileSystem indexableFileSystem, final Pattern pattern) throws IOException {
		final File startDirectory = new File(indexableFileSystem.getPath());
		LOGGER.info("Start directory :" + startDirectory);
		ThreadUtilities.submit(this.toString(), new Runnable() {

			/**
			 * {@inheritDoc}
			 */
			public void run() {
				walkFileSystem(startDirectory);
				finished = Boolean.TRUE;
				LOGGER.info("Finished : " + finished + ", included : " + included + ", excluded : " + excluded + ", size : " + files.size());
			}

			private void walkFileSystem(final File file) {
				try {
					while (files.size() > IConstants.MILLION) {
						ThreadUtilities.sleep(1000);
					}
					if (FileUtilities.isExcluded(file, pattern)) {
						excluded++;
						return;
					}
					if (file.isDirectory()) {
						for (final File child : file.listFiles()) {
							walkFileSystem(child);
						}
					} else {
						included++;
						files.push(file);
					}
				} catch (Exception e) {
					LOGGER.error("Exception walking the file tree : ", e);
				}
			}

		});
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized File getResource() {
		return getResource(5);
	}

	private synchronized File getResource(final int retry) {
		File file = null;
		if (retry > 0) {
			if (files.size() > 0) {
				file = files.pop();
				LOGGER.info("Popping : " + files.size() + ", " + file);
			} else {
				if (!finished) {
					LOGGER.info("Waiting for walker : ");
					ThreadUtilities.sleep(10000);
					file = getResource(retry - 1);
				} else {
					LOGGER.info("No more files : ");
				}
			}
		}
		return file;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResources(final List<File> resources) {
		if (resources == null) {
			return;
		}
		this.files.addAll(resources);
	}

}