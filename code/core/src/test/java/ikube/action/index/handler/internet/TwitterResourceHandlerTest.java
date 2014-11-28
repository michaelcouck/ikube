package ikube.action.index.handler.internet;

import java.io.File;

import ikube.toolkit.FILE;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

import com.google.gson.GsonBuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.IndexableTweets;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 19-06-2013
 */
public class TwitterResourceHandlerTest extends AbstractTest {

	private TwitterResourceHandler twitterResourceHandler;

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		twitterResourceHandler = new TwitterResourceHandler();
		twitterResourceHandler.init();
	}

	@Test
	public void handleResource() throws Exception {
		File file = FILE.findFileRecursively(new File("."), "tweet.json");
		String json = FILE.getContent(file);
		Tweet tweet = new GsonBuilder().disableHtmlEscaping().create().fromJson(json, Tweet.class);
		Document document = new Document();
		IndexableTweets indexableTweets = mock(IndexableTweets.class);

		when(indexableTweets.getCreatedAtField()).thenReturn("created-at");
		when(indexableTweets.getFromUserField()).thenReturn("from-user");
		when(indexableTweets.getTextField()).thenReturn("text-field");
		when(indexableTweets.getUserNameField()).thenReturn("user-name");
		when(indexableTweets.getUserLocationField()).thenReturn("user-location");
		when(indexableTweets.getUserLanguageField()).thenReturn("user-language");
		when(indexableTweets.getContent()).thenReturn("tweet-content");
		when(indexableTweets.getUserUtcOffsetField()).thenReturn("tweet-utc-offset");
		when(indexableTweets.isStored()).thenReturn(Boolean.TRUE);
		when(indexableTweets.isAnalyzed()).thenReturn(Boolean.TRUE);

		document = twitterResourceHandler.handleResource(indexContext, indexableTweets, document, tweet);
		assertEquals("The name of the user must be in the document : ", "nassereem1300", document.get("from-user"));
		assertEquals("The language of the user must be in the document : ", "ar", document.get("user-language"));
	}

}
