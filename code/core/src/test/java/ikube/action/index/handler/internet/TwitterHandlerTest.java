package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.OsUtilities;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 24-04-2013
 */
public class TwitterHandlerTest extends AbstractTest {

    @MockClass(realClass = TwitterResourceProvider.class)
    public static class TwitterResourceProviderMock {

        @Mock
        @SuppressWarnings({"unused"})
        public void $init(final IndexableTweets indexableTweets) throws IOException {
        }

        @Mock
        public Tweet getResource() {
            return mock(Tweet.class);
        }
    }

    private TwitterHandler twitterHandler;
    private IndexableTweets indexableTweets;
    private TwitterResourceHandler twitterResourceHandler;

    @Before
    public void before() throws Exception {
        twitterHandler = new TwitterHandler();
        indexableTweets = mock(IndexableTweets.class);
        twitterResourceHandler = mock(TwitterResourceHandler.class);

        when(indexableTweets.getThreads()).thenReturn(3);
        Deencapsulation.setField(twitterHandler, "twitterResourceHandler", twitterResourceHandler);

        Mockit.setUpMocks(TwitterResourceProviderMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(TwitterResourceProviderMock.class);
    }

    @Test
    public void handleIndexable() throws Exception {
        // TODO: This doesnt' work on CentOs!!!
        if (!OsUtilities.isOs("3.11.0-12-generic")) {
            return;
        }
        final String forkJoinPoolName = indexContext.getName();
        new Thread(new Runnable() {
            public void run() {
                ThreadUtilities.sleep(10000);
                ThreadUtilities.cancelForkJoinPool(forkJoinPoolName);
            }
        }).start();

        final ForkJoinTask<?> forkJoinTask = twitterHandler.handleIndexableForked(indexContext, indexableTweets);
        try {
            ThreadUtilities.executeForkJoinTasks(forkJoinPoolName, indexableTweets.getThreads(), forkJoinTask);
            ThreadUtilities.sleep(15000);
        } catch (final CancellationException e) {
            // Ignore
        }
        verify(twitterResourceHandler, atLeastOnce()).handleResource(any(IndexContext.class), any(IndexableTweets.class), any(Document.class), any(Tweet.class));
    }

    @Test
    public void handleResource() {
        final Tweet tweet = (Tweet) ObjectToolkit.getObject(Tweet.class);
        ObjectToolkit.populateFields(tweet, Boolean.TRUE, 10);
        TwitterProfile twitterProfile = (TwitterProfile) ObjectToolkit.getObject(TwitterProfile.class);
        ObjectToolkit.populateFields(twitterProfile, Boolean.TRUE, 10);
        tweet.setUser(twitterProfile);

        double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
            public void execute() throws Throwable {
                twitterHandler.handleResource(indexContext, indexableTweets, tweet);
            }
        }, "Twitter handler performance : ", 1000, Boolean.TRUE);
        assertTrue(executionsPerSecond > 1000);
    }

}