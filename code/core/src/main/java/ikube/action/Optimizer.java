package ikube.action;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Timer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

/**
 * This class will find all the 'segments.gen' files in a directory, then open index writers on the directories and close them, essentially optimizing all the
 * indexes recursively in a directory, potentially removing all the lock files, and making the index readable and usable.
 * 
 * @author Michael Couck
 * @since 08.02.13
 * @version 01.00
 */
public class Optimizer extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) throws IOException {
		File baseDirectory = new File(indexContext.getIndexDirectoryPath());
		logger.debug("Starting at directory : " + baseDirectory);
		List<File> segmentsFiles = FileUtilities.findFilesRecursively(baseDirectory, new ArrayList<File>(), "segments.gen");
		logger.debug("Segments files : " + segmentsFiles);
		for (final File segmentsFile : segmentsFiles) {
			final File indexDirectory = segmentsFile.getParentFile();
			// Can't optimize this index if it is open or being written to
			if (indexContext.getMultiSearcher() != null || (indexContext.getIndexWriters() != null && indexContext.getIndexWriters().length > 0)) {
				logger.debug("Index already opened : " + indexContext.getIndexDirectoryPath());
				continue;
			}
			Directory directory = null;
			try {
				directory = NIOFSDirectory.open(indexDirectory);
				if (IndexWriter.isLocked(directory)) {
					logger.debug("Index locked : " + indexContext.getIndexDirectoryPath());
					continue;
				}
			} finally {
				directory.close();
			}
			logger.info("Optimizing index : " + indexDirectory);
			Timer.Timed timed = new Timer.Timed() {
				@Override
				public void execute() {
					try {
						IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.FALSE);
						IndexManager.closeIndexWriter(indexWriter);
					} catch (Exception e) {
						logger.error("Exception optimizing index segments file : " + segmentsFile, e);
					}
				}
			};
			long timeTaken = Timer.execute(timed);
			logger.info("Finished optimizing index : " + indexDirectory + ", " + indexDirectory.listFiles().length + ", in : " + (timeTaken / 1000 / 60)
					+ " minutes");
		}
		return Boolean.TRUE;
	}

}