package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.model.IndexableTweets;
import ikube.toolkit.ObjectToolkit;
import org.apache.lucene.document.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05.12.13
 */
public class AnalysisStrategyTest extends AbstractTest {

    private IAnalyzer analyzer;
    /**
     * Class under test.
     */
    private AnalysisStrategy analysisStrategy;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        analyzer = mock(IAnalyzer.class);
        Context context = mock(Context.class);
        when(context.getAnalyzer()).thenReturn(analyzer);
        when(context.getMaxTraining()).thenReturn(1000);

        analysisStrategy = new AnalysisStrategy();
        analysisStrategy.setContext(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aroundProcess() throws Exception {
        Document document = new Document();
        IndexableTweets indexableTweets = mock(IndexableTweets.class);
        Analysis analysis = mock(Analysis.class);

        when(indexableTweets.isStored()).thenReturn(Boolean.TRUE);
        when(indexableTweets.isAnalyzed()).thenReturn(Boolean.TRUE);
        when(indexableTweets.getContent()).thenReturn(IConstants.CONTENT);
        when(analyzer.analyze(any(Analysis.class))).thenReturn(analysis);
        when(analysis.getClazz()).thenReturn(IConstants.NEGATIVE);

        IndexManager.addStringField(IConstants.LANGUAGE, Locale.ENGLISH.getLanguage(), indexableTweets, document);
        IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, indexableTweets, document);

        Tweet tweet = (Tweet) ObjectToolkit.getObject(Tweet.class);
        ObjectToolkit.populateFields(tweet, Boolean.TRUE, 10);

        analysisStrategy.setLanguage(Locale.ENGLISH.getLanguage());
        analysisStrategy.aroundProcess(indexContext, indexableTweets, document, tweet);

        Assert.assertEquals(IConstants.NEGATIVE, document.get(IConstants.CLASSIFICATION_CONFLICT));
    }

}