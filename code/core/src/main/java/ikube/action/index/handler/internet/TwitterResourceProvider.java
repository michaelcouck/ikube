package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableTweets;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.twitter.api.*;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class will use the Spring social module to get tweets from Twitter, at a rate of around 1% of the tweets.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19-06-2013
 */
class TwitterResourceProvider implements IResourceProvider<Tweet> {

    private Logger logger = LoggerFactory.getLogger(TwitterResourceProvider.class);

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private int clones;
    private Stream stream;
    private boolean terminated;

    private Stack<Tweet> tweets;

    /**
     * Constructor takes the configuration for the Twitter account, and initializes the streaming classes that will accept the Twitter data.
     *
     * @param indexableTweets the configuration for the Twitter account, importantly the OAuth login details
     */
    TwitterResourceProvider(final IndexableTweets indexableTweets) throws IOException {
        tweets = new Stack<>();
        clones = indexableTweets.getClones();
        TwitterTemplate twitterTemplate = new TwitterTemplate( //
                indexableTweets.getConsumerKey(), //
                indexableTweets.getConsumerSecret(), //
                indexableTweets.getToken(), //
                indexableTweets.getTokenSecret());
        StreamingOperations streamingOperations = twitterTemplate.streamingOperations();
        StreamListener streamListener = new StreamListener() {

            final AtomicInteger counter = new AtomicInteger();
            final AtomicInteger deleted = new AtomicInteger();

            @Override
            public void onTweet(final Tweet tweet) {
                if (tweets.size() < 10000) {
                    tweets.push(tweet);
                }
                if (counter.incrementAndGet() % 1000 == 0) {
                    logger.info("Tweets received : " + counter.get() + ", tweet cache : " + tweets.size() + ", tweet : " + ToStringBuilder.reflectionToString(tweet));
                }
            }

            @Override
            public void onDelete(final StreamDeleteEvent streamDeleteEvent) {
                if (deleted.incrementAndGet() % 1000 == 0) {
                    logger.info("Deleted : " + deleted.get() + ", event : " + ToStringBuilder.reflectionToString(streamDeleteEvent));
                }
            }

            @Override
            public void onLimit(final int i) {
                logger.info("Limit : " + i);
            }

            @Override
            public void onWarning(final StreamWarningEvent streamWarningEvent) {
                logger.info("Warning : " + streamWarningEvent);
            }
        };
        List<StreamListener> listeners = Collections.singletonList(streamListener);
        stream = streamingOperations.sample(listeners);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTerminated(final boolean terminated) {
        this.terminated = terminated;
        logger.info("Closing twitter stream : ");
        stream.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tweet getResource() {
        if (isTerminated()) {
            return null;
        }
        int retry = 15;
        while (tweets.isEmpty() && retry-- > 0) {
            if (isTerminated()) {
                break;
            }
            synchronized (this) {
                logger.info("Waiting for tweets : " + tweets.size());
                try {
                    wait(15000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                notifyAll();
            }
        }
        if (tweets.isEmpty()) {
            setTerminated(Boolean.TRUE);
            return null;
        }
        return tweets.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResources(final List<Tweet> tweets) {
        if (tweets != null) {
            this.tweets.addAll(tweets);
        }
    }

}