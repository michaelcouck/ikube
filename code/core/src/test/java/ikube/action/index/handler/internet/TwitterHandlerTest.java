package ikube.action.index.handler.internet;

import static ikube.toolkit.ObjectToolkit.populateFields;
import ikube.AbstractTest;
import ikube.model.IndexableInternet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Ignore
public class TwitterHandlerTest extends AbstractTest {
	
	private TwitterHandler twitterHandler;

	public TwitterHandlerTest() {
		super(TwitterHandlerTest.class);
	}
	
	@Before
	public void before() {
		twitterHandler = new TwitterHandler();
	}

	@Test
	public void handleIndexable() throws Exception {
		IndexableInternet indexableInternet = populateFields(IndexableInternet.class, new IndexableInternet(), Boolean.TRUE, 5);
		indexableInternet.setBaseUrl("http://twitter.com/statuses/friends_timeline.xml");
		indexableInternet.setLoginUrl("http://twitter.com/statuses/friends_timeline.xml");
		indexableInternet.setUserid("michaelcouck");
		indexableInternet.setPassword("caherline");
		twitterHandler.handleIndexable(indexContext, indexableInternet);
	}

}