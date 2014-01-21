package ikube.action.index.handler.internet;

import com.google.gson.Gson;
import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;
import ikube.toolkit.ThreadUtilities;
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

/**
 * This class will use the Spring social module to get tweets from Twitter, at a rate of around 1% of the tweets.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19.06.13
 */
class TwitterResourceProvider implements IResourceProvider<Tweet>, StreamListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private int clones;
    private Stack<Tweet> tweets;

    /**
     * The directories where the stack will be persisted to the file system.
     */
    private File tweetsDirectory;

    /**
     * Constructor takes the configuration for the Twitter account, and initializes the streaming classes that will accept the Twitter data.
     *
     * @param indexableTweets the configuration for the Twitter account, importantly the OAuth login details
     * @throws IOException
     */
    TwitterResourceProvider(final IndexableTweets indexableTweets) throws IOException {
        IndexContext indexContext = (IndexContext) indexableTweets.getParent();
        clones = indexableTweets.getClones();
        tweets = new Stack<>();
        tweetsDirectory = FileUtilities.getOrCreateDirectory(new File(indexContext.getIndexDirectoryPath(), "tweets"));
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
                wait(1000);
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
    public void onTweet(final Tweet tweet) {
        persistResources(tweet);
        if (tweets.size() < IConstants.MILLION) {
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
        logger.info("Tweets limited : " + numberOfLimitedTweets);
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
        logger.info("Tweet warning : " + warnEvent.getCode());
    }

    Stack<Tweet> stack = new Stack<>();

    void persistResources(final Tweet... tweets) {
        Collections.addAll(stack, tweets);
        if (stack.size() > 10000) {
            try {
                File latestDirectory = FileUtilities.getOrCreateDirectory(new File(tweetsDirectory, Long.toString(System.currentTimeMillis())));
                Gson gson = new Gson();
                for (final Tweet tweet : stack) {
                    String string = gson.toJson(tweet);
                    File output = new File(latestDirectory, Long.toString(System.currentTimeMillis()) + ".json");
                    File outputFile = FileUtilities.getOrCreateFile(output);
                    FileUtilities.setContents(outputFile, string.getBytes());
                    ThreadUtilities.sleep(1);
                }
            } catch (Exception e) {
                logger.error(null, e);
            }
            stack.clear();
        }
    }

}