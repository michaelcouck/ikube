package ikube.toolkit;

import ikube.Ikube;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will find all the 'segments.gen' files in a directory, then open index writers on the directories and close them, essentially
 * optimizing all the indexes recursively in a directory, potentially removing all the lock files, and making the index readable and
 * usable.
 * 
 * @author Michael Couck
 * @since 08.02.13
 * @version 01.00
 */
public final class Optimizer {

	static {
		Logging.configure();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Ikube.class);

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		final IndexContext<?> indexContext = new IndexContext<Indexable>();
		indexContext.setMergeFactor(10000);
		indexContext.setBufferSize(256);
		indexContext.setBufferedDocs(10000);
		indexContext.setCompoundFile(Boolean.TRUE);
		for (final String basePath : args) {
			try {
				File baseDirectory = new File(basePath);
				List<File> segmentsFiles = FileUtilities.findFilesRecursively(baseDirectory, new ArrayList<File>(), "segments.gen");
				for (final File segmentsFile : segmentsFiles) {
					final File indexDirectory = segmentsFile.getParentFile();
					LOGGER.info("Optimizing index : " + indexDirectory);
					Timer.Timed timed = new Timer.Timed() {
						@Override
						public void execute() {
							try {
								IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.FALSE);
								IndexManager.closeIndexWriter(indexWriter);
							} catch (Exception e) {
								LOGGER.error("Exception optimizing index segments file : " + segmentsFile, e);
							}
						}
					};
					long timeTaken = Timer.execute(timed);
					LOGGER.info("Finished optimizing index : " + indexDirectory + ", in : " + (timeTaken / 1000 / 60) + " minutes");
				}
			} catch (Exception e) {
				LOGGER.error("Usage : java -jar ikube-tools.jar ikube.toolkit.Optimizer [lucene-index-to-optimize lucene-index-to-optimize lucene-index-to-optimize...]");
				LOGGER.error(null, e);
			}
		}
	}

}