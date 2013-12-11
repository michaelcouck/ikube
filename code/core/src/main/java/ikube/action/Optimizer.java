package ikube.action;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexWriter;

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
	 */
	@Override
	public boolean internalExecute(final IndexContext<?> indexContext) {
		File baseDirectory = new File(indexContext.getIndexDirectoryPath());
		List<File> segmentsFiles = FileUtilities.findFilesRecursively(baseDirectory, new ArrayList<File>(), "segments.gen");
		for (final File segmentsFile : segmentsFiles) {
			final File indexDirectory = segmentsFile.getParentFile();
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