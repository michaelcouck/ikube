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
import org.mockito.Mockito;
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
        analyzer = Mockito.mock(IAnalyzer.class);
        Context context = Mockito.mock(Context.class);
        Mockito.when(context.getAnalyzer()).thenReturn(analyzer);
        Mockito.when(context.getMaxTraining()).thenReturn(100);

        classifierTrainingStrategy = new ClassifierTrainingStrategy();
        classifierTrainingStrategy.setContext(context);
        classifierTrainingStrategy.initialize();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aroundProcess() throws Exception {
        Document document = new Document();
        final IndexableTweets indexableTweets = Mockito.mock(IndexableTweets.class);
        Mockito.when(indexableTweets.isStored()).thenReturn(Boolean.TRUE);
        Mockito.when(indexableTweets.isAnalyzed()).thenReturn(Boolean.TRUE);
        Mockito.when(indexableTweets.getContent()).thenReturn(IConstants.CONTENT);

        IndexManager.addStringField(IConstants.LANGUAGE, Locale.ENGLISH.getLanguage(), indexableTweets, document);
        IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, indexableTweets, document);

        final Tweet tweet = (Tweet) ObjectToolkit.getObject(Tweet.class);
        ObjectToolkit.populateFields(tweet, Boolean.TRUE, 10);

        classifierTrainingStrategy.setLanguage(Locale.ENGLISH.getLanguage());
        classifierTrainingStrategy.aroundProcess(indexContext, indexableTweets, document, tweet);
        Mockito.verify(analyzer).train(Mockito.any(Object[].class));

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