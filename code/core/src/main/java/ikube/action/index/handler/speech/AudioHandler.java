package ikube.action.index.handler.speech;

import ikube.action.index.handler.IndexableHandler;
import ikube.action.index.handler.ResourceHandlerBase;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;

/**
 * @author Michael Couck
 * @since 21.04.13
 * @version 01.00
 */
public class AudioHandler extends IndexableHandler<IndexableInternet> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableInternet indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		ConfigurationManager cm = new ConfigurationManager(this.getClass().getResource("helloworld.config.xml"));
		Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
		recognizer.allocate();
		AudioFileDataSource dataSource = (AudioFileDataSource) cm.lookup("audioFileDataSource");
		// dataSource.setAudioFile(audioFile, null);
		return futures;
	}
	
	@Override
	protected void handleResource(final IndexContext<?> indexContext, final Indexable<?> indexable, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
	}

}