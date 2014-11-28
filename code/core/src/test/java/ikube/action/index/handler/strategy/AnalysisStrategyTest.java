package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.model.IndexableTweets;
import ikube.toolkit.OBJECT;
import mockit.Deencapsulation;
import org.apache.lucene.document.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

import java.util.Locale;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05-12-2013
 */
public class AnalysisStrategyTest extends AbstractTest {

    private IAnalyzer analyzer;
    /**
     * Class under test.
     */
    private AnalysisStrategy analysisStrategy;
    private IAnalyticsService analyticsService;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        analyzer = mock(IAnalyzer.class);
        analyticsService = mock(IAnalyticsService.class);
        Context context = mock(Context.class);
        when(context.getAnalyzer()).thenReturn(analyzer);
        when(context.getMaxTrainings()).thenReturn(new int[]{1000});

        analysisStrategy = new AnalysisStrategy();
        analysisStrategy.setContext(context);

        Deencapsulation.setField(analysisStrategy, "analyticsService", analyticsService);
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
        when(analyzer.analyze(any(Context.class), any(Analysis.class))).thenReturn(analysis);
        when(analysis.getClazz()).thenReturn(IConstants.NEGATIVE);
        when(analyticsService.analyze(any(Analysis.class))).thenReturn(analysis);

        IndexManager.addStringField(IConstants.LANGUAGE, Locale.ENGLISH.getLanguage(), indexableTweets, document);
        IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, indexableTweets, document);

        Tweet tweet = (Tweet) OBJECT.getObject(Tweet.class);
        OBJECT.populateFields(tweet, Boolean.TRUE, 10);

        analysisStrategy.aroundProcess(indexContext, indexableTweets, document, tweet);

        Assert.assertEquals(IConstants.NEGATIVE, document.get(IConstants.CLASSIFICATION_CONFLICT));
    }

}