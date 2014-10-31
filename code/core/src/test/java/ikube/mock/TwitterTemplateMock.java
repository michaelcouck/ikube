package ikube.mock;

import mockit.Mock;
import mockit.MockClass;
import org.springframework.social.twitter.api.StreamingOperations;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import static org.mockito.Mockito.mock;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-07-2013
 */
@SuppressWarnings("UnusedDeclaration")
@MockClass(realClass = TwitterTemplate.class)
public class TwitterTemplateMock {

    @Mock
    public void $init(
            final String consumerKey,
            final String consumerSecret,
            final String accessToken,
            final String accessTokenSecret) {
    }

    @Mock
    public StreamingOperations streamingOperations() {
        return mock(StreamingOperations.class);
    }

}
