package ikube.action.index.handler.speech;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableAudio;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;

/**
 * @author Michael Couck
 * @since 21.04.13
 * @version 01.00
 */
public class AudioHandler extends IndexableHandler<IndexableAudio> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unused")
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableAudio indexable) throws Exception {
		ConfigurationManager cm = new ConfigurationManager(this.getClass().getResource("helloworld.config.xml"));
		Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
		recognizer.allocate();
		AudioFileDataSource dataSource = (AudioFileDataSource) cm.lookup("audioFileDataSource");
		// dataSource.setAudioFile(audioFile, null);
		IResourceProvider<Object> twitterResourceProvider = new IResourceProvider<Object>() {
			@Override
			public Object getResource() {
				return null;
			}

			@Override
			public void setResources(List<Object> resources) {
			}
		};
		return getRecursiveAction(indexContext, indexable, twitterResourceProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableAudio indexable, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		return null;
	}

}