package ikube.toolkit;

import ikube.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-07-2012
 */
public class UdpBroadcasterTest extends AbstractTest {

    @Test
    public void main() {
        UdpBroadcaster.main(new String[]{"10"});
        Assert.assertTrue(UdpBroadcaster.MESSAGES_SENT > 0);
        Assert.assertTrue(UdpBroadcaster.MESSAGES_RECEIVED > 0);
    }

}
