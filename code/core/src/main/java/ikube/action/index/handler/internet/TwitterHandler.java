package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;
import ikube.toolkit.STRING;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Tweet;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * This class is the configurable part of the Twitter source of data. It combines the resource provider
 * {@link TwitterResourceProvider} and the {@link TwitterResourceHandler} to get around 1% of the tweets
 * from Twitter. Generally strategies are chained to the processing to calculate the sentiment of the tweets.
 * This additional data is used to enrich the index, which can then be used to generate time line reports
 * on the twitter data.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-04-2013
 */
public class TwitterHandler extends IndexableHandler<IndexableTweets> {

    private static final char[] EXCLUSIONS = new char[0];

    @Autowired
    private TwitterResourceHandler twitterResourceHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(final IndexContext indexContext, final IndexableTweets indexableTweets) throws Exception {
        IResourceProvider<Tweet> twitterResourceProvider = new TwitterResourceProvider(indexableTweets);
        return getRecursiveAction(indexContext, indexableTweets, twitterResourceProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("StringBufferReplaceableByString")
    protected List<?> handleResource(final IndexContext indexContext, final IndexableTweets indexableTweets, final Object resource) {
        Tweet tweet = (Tweet) resource;

        // Build the primary field in the index
        StringBuilder builder = new StringBuilder();
        builder.append(tweet.getFromUserId());
        builder.append(" ");
        builder.append(tweet.getFromUser());
        builder.append(" ");
        // Strip the punctuation, we don't need it
        builder.append(tweet.getText());

        indexableTweets.setContent(STRING.stripToAlphaNumeric(builder.toString(), EXCLUSIONS));
        // And put the data in the index
        try {
            twitterResourceHandler.handleResource(indexContext, indexableTweets, new Document(), resource);
        } catch (final Exception e) {
            handleException(indexableTweets, e, "Exception handling Twitter resource : " + tweet);
        }
        return null;
    }

}