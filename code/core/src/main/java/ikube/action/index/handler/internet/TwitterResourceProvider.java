package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;
import ikube.toolkit.SerializationUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.twitter.api.*;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static ikube.toolkit.FileUtilities.*;

/**
 * This class will use the Spring social module to get tweets from Twitter, at a rate of around 1% of the tweets.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19-06-2013
 */
class TwitterResourceProvider implements IResourceProvider<Tweet>, StreamListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static int STACK_SIZE = IConstants.ONE_THOUSAND;

    private int clones;
    private Stack<Tweet> stack;
    private Stack<Tweet> tweets;
    private boolean persistTweets;

    /**
     * The directories where the stack will be persisted to the file system.
     */
    private File tweetsDirectory;

    /**
     * Constructor takes the configuration for the Twitter account, and initializes the streaming classes that will accept the Twitter data.
     *
     * @param indexableTweets the configuration for the Twitter account, importantly the OAuth login details
     */
    TwitterResourceProvider(final IndexableTweets indexableTweets) throws IOException {
        stack = new Stack<>();
        tweets = new Stack<>();
        clones = indexableTweets.getClones();
        IndexContext indexContext = (IndexContext) indexableTweets.getParent();
        persistTweets = indexableTweets.isPersistTweets();
        tweetsDirectory = getOrCreateDirectory(new File(indexContext.getIndexDirectoryPath(), "tweets"));
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
                logger.debug("Waiting for tweets : ");
                wait(10000);
            } catch (final InterruptedException e) {
                logger.error(null, e);
            } catch (final Exception e) {
                logger.error(null, e);
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
        if (tweets.size() < STACK_SIZE) {
            if (tweets.size() % 1000 == 0) {
                logger.info("Tweets : " + tweets.size());
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
        persistResources(tweet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLimit(final int numberOfLimitedTweets) {
        logger.warn("Tweets limited : " + numberOfLimitedTweets);
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
        logger.warn("Tweet warning : " + warnEvent.getCode());
    }

    /**
     * This method persists all the tweets to the file system that are in the stack. The stack is 'emptied' onto
     * the file system, and then cleaned. All the tweets on the current stack are persisted to one directory, so typically
     * each directory will have 'STACK_SIZE' tweets in it.
     *
     * @param tweets the tweets to persist to the file system in Json format
     */
    synchronized void persistResources(final Tweet... tweets) {
        if (!persistTweets) {
            return;
        }
        logger.info("Persisting tweets : ");
        Collections.addAll(stack, tweets);
        if (stack.size() > STACK_SIZE) {
            try {
                File latestDirectory = getOrCreateDirectory(
                        new File(tweetsDirectory, Long.toString(System.currentTimeMillis())));
                for (final Tweet tweet : stack) {
                    String string = IConstants.GSON.toJson(tweet);
                    File output = new File(latestDirectory, Long.toString(System.currentTimeMillis()) + ".json");
                    File outputFile = getOrCreateFile(output);
                    setContents(outputFile, string.getBytes());
                }
            } catch (final Exception e) {
                logger.error("Exception persisting the tweets to the file system : ", e);
            }
            stack.clear();
        }
    }

}