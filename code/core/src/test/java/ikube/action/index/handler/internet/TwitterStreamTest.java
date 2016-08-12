package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.toolkit.THREAD;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.social.twitter.api.*;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-07-2013
 */
@Ignore
public class TwitterStreamTest extends AbstractTest {

    @Test
    public void streamTweets() {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger deleted = new AtomicInteger();
        TwitterTemplate twitterTemplate = new TwitterTemplate( //
                "WSQs59mLxb9mGFcPKtJMyw", //
                "lHfsHqI8IrdpRINHTsZwH7cnqz8pgN7uNt9Jz8bE66Q", //
                "2257682162-Z4HFFXnmucyRS3TuCTPE3dOpr2qEYuaE5WiExOp", //
                "6YdLo8Udm5SVMO3WGX1753xnKdGp5eBalQBtcKTb19CpI");
        StreamingOperations streamingOperations = twitterTemplate.streamingOperations();
        StreamListener streamListener = new StreamListener() {
            @Override
            public void onTweet(Tweet tweet) {
                if (counter.incrementAndGet() % 1000 == 0) {
                    logger.info("Tweets : " + counter.get() + ", tweet : " + ToStringBuilder.reflectionToString(tweet));
                }
            }

            @Override
            public void onDelete(StreamDeleteEvent streamDeleteEvent) {
                if (deleted.incrementAndGet() % 1000 == 0) {
                    logger.info("Deleted : " + deleted.get() + ", event : " + ToStringBuilder.reflectionToString(streamDeleteEvent));
                }
            }

            @Override
            public void onLimit(int i) {
                logger.info("Limit : " + i);
            }

            @Override
            public void onWarning(StreamWarningEvent streamWarningEvent) {
                logger.info("Warning : " + streamWarningEvent);
            }
        };
        List<StreamListener> listeners = Collections.singletonList(streamListener);
        Stream stream = streamingOperations.sample(listeners);

        THREAD.sleep(60000000);
    }

}