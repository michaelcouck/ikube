package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.analytics.IAnalyzer;
import ikube.model.Context;
import ikube.model.IndexableTweets;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.PerformanceTester;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.springframework.social.twitter.api.Tweet;

import java.util.Locale;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05.12.13
 */
public class ClassifierTrainingStrategyTest extends AbstractTest {

    @SuppressWarnings("rawtypes")
    private IAnalyzer analyzer;
    /**
     * Class under test.
     */
    private ClassifierTrainingStrategy classifierTrainingStrategy;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        analyzer = mock(IAnalyzer.class);
        Context context = mock(Context.class);
        when(context.getAnalyzer()).thenReturn(analyzer);
        when(context.getMaxTraining()).thenReturn(100);

        classifierTrainingStrategy = new ClassifierTrainingStrategy();
        classifierTrainingStrategy.setContext(context);
        classifierTrainingStrategy.initialize();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aroundProcess() throws Exception {
        Document document = new Document();
        final IndexableTweets indexableTweets = mock(IndexableTweets.class);
        when(indexableTweets.isStored()).thenReturn(Boolean.TRUE);
        when(indexableTweets.isAnalyzed()).thenReturn(Boolean.TRUE);
        when(indexableTweets.getContent()).thenReturn(IConstants.CONTENT);
        when(analyzer.sizeForClassOrCluster(any())).thenReturn(1);

        IndexManager.addStringField(IConstants.LANGUAGE, Locale.ENGLISH.getLanguage(), indexableTweets, document);
        IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, indexableTweets, document);

        final Tweet tweet = (Tweet) ObjectToolkit.getObject(Tweet.class);
        ObjectToolkit.populateFields(tweet, Boolean.TRUE, 10);

        classifierTrainingStrategy.setLanguage(Locale.ENGLISH.getLanguage());
        classifierTrainingStrategy.aroundProcess(indexContext, indexableTweets, document, tweet);
        verify(analyzer).train(any(Object[].class));

        int iterations = 11;
        PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() {
                try {
                    classifierTrainingStrategy.aroundProcess(indexContext, indexableTweets, new Document(), tweet);
                } catch (Exception e) {
                    logger.error(null, e);
                }
            }
        }, "Classifier training performance : ", iterations, Boolean.TRUE);
    }

}