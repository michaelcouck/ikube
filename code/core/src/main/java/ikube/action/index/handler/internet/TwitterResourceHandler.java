package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;
import org.apache.lucene.document.Document;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TwitterProfile;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class simply takes the specific data from Twitter and adds it to the index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19-06-2013
 */
public class TwitterResourceHandler extends ResourceHandler<IndexableTweets> {

    private AtomicLong counter;

    public void init() {
        counter = new AtomicLong(0);
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception
     */
    @Override
    public Document handleResource(final IndexContext indexContext, final IndexableTweets indexableTweets, final Document document,
                                   final Object resource) throws Exception {
        Tweet tweet = (Tweet) resource;

        // This is the unique id of the resource to be able to delete it
        String tweetId = Long.toString(tweet.getId());
        String createdAtField = indexableTweets.getCreatedAtField();
        String fromUserField = indexableTweets.getFromUserField();
        String textField = indexableTweets.getTextField();

        // NOTE to self: To be able to delete using the index writer the identifier field must be non analyzed and non tokenized/vectored!
        // IndexManager.addStringField(IConstants.ID, tweetId, document, Store.YES, Index.NOT_ANALYZED, TermVector.NO);
        IndexManager.addNumericField(IConstants.ID, tweetId, document, Boolean.TRUE, 0);
        IndexManager.addNumericField(createdAtField, Long.toString(tweet.getCreatedAt().getTime()), document, Boolean.TRUE, 0);
        IndexManager.addStringField(fromUserField, tweet.getFromUser(), indexableTweets, document);
        IndexManager.addStringField(textField, indexableTweets.getContent().toString(), indexableTweets, document);

        handleProfile(indexableTweets, document, tweet);

        if (counter.getAndIncrement() % 10000 == 0) {
            logger.info("Document : " + document);
        }

        return super.handleResource(indexContext, indexableTweets, document, resource);
    }

    void handleProfile(final IndexableTweets indexableTweets, final Document document, final Tweet tweet) {
        String userNameField = indexableTweets.getUserNameField();
        String userScreenNameField = indexableTweets.getUserScreenNameField();
        String userLocationField = indexableTweets.getUserLocationField();
        String userTimeZoneField = indexableTweets.getUserTimeZoneField();
        String userUtcOffsetField = indexableTweets.getUserUtcOffsetField();
        String userLanguageField = indexableTweets.getUserLanguageField();

        TwitterProfile twitterProfile = tweet.getUser();

        if (twitterProfile != null) {
            String userName = twitterProfile.getName();
            String userScreenName = twitterProfile.getScreenName();
            String userLocation = twitterProfile.getLocation();
            String userTimeZone = twitterProfile.getTimeZone();
            int userUtcOffset = twitterProfile.getUtcOffset();
            String userLanguage = twitterProfile.getLanguage();

            IndexManager.addStringField(userNameField, userName, indexableTweets, document);
            IndexManager.addStringField(userScreenNameField, userScreenName, indexableTweets, document);
            IndexManager.addStringField(userLocationField, userLocation, indexableTweets, document);
            IndexManager.addStringField(userTimeZoneField, userTimeZone, indexableTweets, document);
            IndexManager.addNumericField(userUtcOffsetField, Integer.toString(userUtcOffset), document, Boolean.TRUE, 0);
            IndexManager.addStringField(userLanguageField, userLanguage, indexableTweets, document);
        }
    }

}