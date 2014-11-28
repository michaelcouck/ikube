package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableTweets;
import ikube.toolkit.OBJECT;
import ikube.toolkit.PERFORMANCE;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

import java.io.IOException;
import java.util.concurrent.ForkJoinTask;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Spy
    @InjectMocks
    private TwitterHandler twitterHandler;
    @org.mockito.Mock
    private IndexableTweets indexableTweets;
    @org.mockito.Mock
    private TwitterResourceHandler twitterResourceHandler;

    @Before
    public void before() throws Exception {
        Mockit.setUpMocks(TwitterResourceProviderMock.class);
        when(indexableTweets.getThreads()).thenReturn(0);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(TwitterResourceProviderMock.class);
    }

    @Test
    public void handleIndexable() throws Exception {
        ForkJoinTask<?> forkJoinTask = twitterHandler.handleIndexableForked(indexContext, indexableTweets);
        assertNotNull(forkJoinTask);
    }

    @Test
    public void handleResource() {
        final Tweet tweet = (Tweet) OBJECT.getObject(Tweet.class);
        OBJECT.populateFields(tweet, Boolean.TRUE, 10);
        TwitterProfile twitterProfile = (TwitterProfile) OBJECT.getObject(TwitterProfile.class);
        OBJECT.populateFields(twitterProfile, Boolean.TRUE, 10);
        tweet.setUser(twitterProfile);

        double executionsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() throws Throwable {
                twitterHandler.handleResource(indexContext, indexableTweets, tweet);
            }
        }, "Twitter handler performance : ", 1000, Boolean.TRUE);
        assertTrue(executionsPerSecond > 1000);
    }

}