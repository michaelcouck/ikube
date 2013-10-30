package ikube.action.index.handler.filesystem;

import ikube.IConstants;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableDictionary;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 01.10.11
 * @version 01.00
 */
@Deprecated
public class IndexableDictionaryHandler extends IndexableHandler<IndexableDictionary> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableDictionary indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		try {
			File spellingIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			Directory directory = FSDirectory.open(spellingIndexDirectory);
			@SuppressWarnings("resource")
			final SpellChecker spellChecker = new SpellChecker(directory);
			for (int i = 0; i < indexable.getThreads(); i++) {
				Runnable runnable = new Runnable() {
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
								IndexWriterConfig indexWriterConfig = new IndexWriterConfig(IConstants.VERSION, new StemmingAnalyzer());
								spellChecker.indexDictionary(new PlainTextDictionary(inputStream), indexWriterConfig, Boolean.TRUE);
							} catch (Exception e) {
								handleException(indexable, e);
							} finally {
								FileUtilities.close(inputStream);
							}
						}
					}
				};
				Future<?> future = ThreadUtilities.submit(indexContext.getIndexName(), runnable);
				futures.add(future);
			}
		} catch (Exception e) {
			handleException(indexable, e);
		}
		return futures;
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableDictionary indexableDictionary, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		return null;
	}

}