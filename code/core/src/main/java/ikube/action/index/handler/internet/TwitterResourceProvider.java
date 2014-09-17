package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableTweets;
import ikube.toolkit.SerializationUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.twitter.api.*;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * This class will use the Spring social module to get tweets from Twitter, at a rate of around 1% of the tweets.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19-06-2013
 */
class TwitterResourceProvider implements IResourceProvider<Tweet>, StreamListener {

    private static Logger LOGGER = LoggerFactory.getLogger(TwitterResourceProvider.class);
    
    private int clones;
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
        StreamListener streamListener = this;
        List<StreamListener> listeners = Arrays.asList(streamListener);
        streamingOperations.sample(listeners);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Tweet getResource() {
        while (tweets.isEmpty()) {
            try {
                LOGGER.debug("Waiting for tweets : ");
                wait(10000);
            } catch (final InterruptedException e) {
                LOGGER.error(null, e);
            } catch (final Exception e) {
                LOGGER.error(null, e);
                return null;
            }
        }
        return tweets.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setResources(final List<Tweet> tweets) {
        if (tweets != null) {
            this.tweets.addAll(tweets);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onTweet(final Tweet tweet) {
        if (tweets.size() < IConstants.ONE_THOUSAND) {
            if (tweets.size() % 1000 == 0) {
                LOGGER.info("Tweets : " + tweets.size());
            }
            tweets.push(tweet);
            if (this.clones > 0) {
                int clones = this.clones;
                do {
                    Tweet clone = (Tweet) SerializationUtilities.clone(tweet);
                    tweets.push(clone);
                } while (--clones > 0);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLimit(final int numberOfLimitedTweets) {
        LOGGER.warn("Tweets limited : " + numberOfLimitedTweets);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDelete(final StreamDeleteEvent deleteEvent) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWarning(final StreamWarningEvent warnEvent) {
        LOGGER.warn("Tweet warning : " + warnEvent.getCode());
    }

}