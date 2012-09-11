package ikube.listener;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.Map;
import java.util.Random;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * This listener will check the size of an index and start a new one if it gets over the maximum allowed size. This is because over NFS the
 * size of the Lucene index is important as there is a seek in the file, and aparently this is not well supported over NFS and the
 * performance degrades with the size of the index, i.e. over for example 10 gig.
 * 
 * @author Michael Couck
 * @since 29.08.12
 * @version 01.00
 */
public class IndexSizeListener implements IListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexSizeListener.class);

	@Value("${max.index.size}")
	private long maxIndexSize;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void handleNotification(Event event) {
		if (Event.TIMER.equals(event.getType())) {
			Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
			for (Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
				IndexContext<?> indexContext = mapEntry.getValue();
				IndexWriter indexWriter = indexContext.getIndexWriter();
				if (indexWriter == null) {
					LOGGER.info("Not indexing : " + indexWriter);
					return;
				}
				long meg = 1024 * 1000;
				long indexSize = indexContext.getLastSnapshot().getIndexSize();
				LOGGER.info("Index size : " + indexSize + ", " + maxIndexSize + ", " + (indexSize / meg < maxIndexSize));
				if (indexSize / meg < maxIndexSize) {
					return;
				}
				FSDirectory directory = (FSDirectory) indexWriter.getDirectory();
				File indexDirectory = directory.getFile();
				try {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(indexDirectory.getParentFile().getAbsolutePath());
					stringBuilder.append(IConstants.SEP);
					stringBuilder.append(indexDirectory.getName());
					stringBuilder.append(".");
					stringBuilder.append(Math.abs(new Random().nextInt()));

					File newIndexDirectory = FileUtilities.getFile(stringBuilder.toString(), Boolean.TRUE);
					LOGGER.info("Starting new index : " + newIndexDirectory);
					IndexWriter newIndexWriter = IndexManager.openIndexWriter(indexContext, newIndexDirectory, true);

					int retry = 10;
					boolean switched = false;
					while (retry-- > 0) {
						// Because this variable is volatile we can 'miss' a write
						// so we have to try a few times to be sure it is written to memory
						indexContext.setIndexWriter(newIndexWriter);
						if (indexContext.getIndexWriter() == newIndexWriter) {
							switched = true;
							break;
						}
						ThreadUtilities.sleep(100);
					}

					if (switched) {
						LOGGER.info("Switched to the new index writer : ");
						IndexManager.closeIndexWriter(indexWriter);
					} else {
						LOGGER.warn("Didn't switch to the new index writer, will try again next notification : ");
					}
				} catch (Exception e) {
					LOGGER.error("Exception starting a new index : ", e);
				}
			}
		}
	}

}