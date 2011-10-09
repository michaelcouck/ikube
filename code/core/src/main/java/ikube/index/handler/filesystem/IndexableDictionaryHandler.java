package ikube.index.handler.filesystem;

import ikube.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableDictionary;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 01.10.11
 * @version 01.00
 */
public class IndexableDictionaryHandler extends IndexableHandler<IndexableDictionary> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Thread> handle(final IndexContext<?> indexContext, final IndexableDictionary indexable) throws Exception {
		try {
			List<Thread> threads = new ArrayList<Thread>();
			File spellingIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			Directory directory = FSDirectory.open(spellingIndexDirectory);
			final SpellChecker spellChecker = new SpellChecker(directory);
			for (int i = 0; i < getThreads(); i++) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						File[] files = new File(indexable.getPath()).listFiles();
						if (files == null) {
							return;
						}
						logger.info("Dictionary file : " + Arrays.asList(files));
						for (File file : files) {
							if (file.isDirectory()) {
								continue;
							}
							InputStream inputStream = null;
							try {
								logger.info("Indexing dictionary file : " + file);
								inputStream = new FileInputStream(file);
								spellChecker.indexDictionary(new PlainTextDictionary(inputStream));
							} catch (Exception e) {
								logger.error("Exception indexing the dictionary file : " + file, e);
							} finally {
								FileUtilities.close(inputStream);
							}
						}
					}
				}, this.getClass().getSimpleName() + "." + i);
				threads.add(thread);
			}
			for (Thread thread : threads) {
				thread.start();
			}
			return threads;
		} catch (Exception e) {
			logger.error("Exception starting the file system indexer threads : ", e);
		}
		return Arrays.asList();
	}

}
