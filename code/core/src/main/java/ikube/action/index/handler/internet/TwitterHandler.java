package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

/**
 * @author Michael Couck
 * @since 24.04.13
 * @version 01.00
 */
public class TwitterHandler extends IndexableHandler<IndexableTweets> {

	@Autowired
	private TwitterResourceHandler twitterResourceHandler;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForkJoinTask<?> handleIndexableForked(final IndexContext<?> indexContext, final IndexableTweets indexableTweets) throws Exception {
		IResourceProvider<Tweet> twitterResourceProvider = new TwitterResourceProvider(indexableTweets);
		return getRecursiveAction(indexContext, indexableTweets, twitterResourceProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableTweets indexableTweets, final Object resource) {
		Tweet tweet = (Tweet) resource;
		TwitterProfile twitterProfile = tweet.getUser();
		String location = twitterProfile.getLocation();
		// Based on the universal time offset, the time zone and the language we should be able to
		// guess the exact location of the tweet, but for now we'll just hope that the location is not null
		// and indeed filled in correctly
		twitterProfile.getUtcOffset();
		twitterProfile.getTimeZone();
		// We add the location to the indexable because the geospatial
		// strategy needs this to guess the geo-coordinates of the tweet
		indexableTweets.setContent(location);
		// And put the data in the index
		twitterResourceHandler.handleResource(indexContext, indexableTweets, new Document(), resource);
		// We could get more tweets here I guess
		return null;
	}

}