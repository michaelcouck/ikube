package ikube.toolkit;

import ikube.Ikube;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.io.File;

import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Optimizer {
	
	static {
		Logging.configure();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Ikube.class);

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		IndexContext<?> indexContext = new IndexContext<Indexable>();
		indexContext.setMergeFactor(10000);
		indexContext.setBufferSize(256);
		indexContext.setBufferedDocs(10000);
		indexContext.setCompoundFile(Boolean.TRUE);
		for (final String directoryPath : args) {
			try {
				File indexDirectory = new File(directoryPath);
				IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, indexDirectory, Boolean.FALSE);
				IndexManager.closeIndexWriter(indexWriter);
			} catch (Exception e) {
				LOGGER.error("Usage : java -jar ikube-tools.jar ikube.toolkit.Optimizer [lucene-index-to-optimize lucene-index-to-optimize lucene-index-to-optimize...]");
				LOGGER.error(null, e);
			}
		}
	}

}
