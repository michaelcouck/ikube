package ikube.action.index.handler.speech;

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableAudio;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * This class is an investigation into speech to text for indexing and analysis. It is not complete
 * at the time of writing(17-08-2014), and the technology converting speech to text was not stable enough
 * to extract any usable dependable information.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-04-2013
 */
public class AudioHandler extends IndexableHandler<IndexableAudio> {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unused")
    public ForkJoinTask<?> handleIndexableForked(final IndexContext indexContext, final IndexableAudio indexable) throws Exception {
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
    protected List<?> handleResource(final IndexContext indexContext, final IndexableAudio indexable, final Object resource) {
        logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
        return null;
    }

}