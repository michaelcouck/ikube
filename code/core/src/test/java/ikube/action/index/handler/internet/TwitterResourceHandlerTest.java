package ikube.action.index.handler.internet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.model.IndexableTweets;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import mockit.Deencapsulation;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

import com.google.gson.GsonBuilder;

public class TwitterResourceHandlerTest extends AbstractTest {

	private ISearcherService searcherService;
	private TwitterResourceHandler twitterResourceHandler;

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		searcherService = mock(ISearcherService.class);
		File file = FileUtilities.findFileRecursively(new File("."), "geospatial.results.xml");
		String xml = FileUtilities.getContents(file, Integer.MAX_VALUE).toString();
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		when(searcherService.search(any(String.class), any(String[].class), any(String[].class), anyBoolean(), anyInt(), anyInt())).thenReturn(results);
		twitterResourceHandler = new TwitterResourceHandler();
		// twitterResourceHandler.init();
		Deencapsulation.setField(twitterResourceHandler, searcherService);
	}

	@Test
	public void handleResource() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "tweet.json");
		String json = FileUtilities.getContent(file);
		Tweet tweet = new GsonBuilder().disableHtmlEscaping().create().fromJson(json, Tweet.class);
		Document document = new Document();
		IndexableTweets indexableTweets = mock(IndexableTweets.class);

		when(indexableTweets.getCreatedAtField()).thenReturn("created-at");
		when(indexableTweets.getFromUserField()).thenReturn("from-user");
		when(indexableTweets.getTextField()).thenReturn("text-field");
		when(indexableTweets.getUserNameField()).thenReturn("user-name");
		when(indexableTweets.getUserLocationField()).thenReturn("user-location");
		when(indexableTweets.getContent()).thenReturn("tweet-content");

		twitterResourceHandler.handleResource(indexContext, indexableTweets, document, tweet);
	}

}
