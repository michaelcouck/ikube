package ikube.network;

import ikube.AbstractTest;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * This class contains methods for scanning the network.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-10-2014
 */
public class NetworkUtilitiesTest extends AbstractTest {

    @Test
    public void scanNetwork() throws Exception {
        String[] reachableMachines = NetworkUtilities.scanNetwork("192.168.1.255/24", 250);
        logger.error(Arrays.toString(reachableMachines));
        assertTrue("Must be at least the local machine and the router : ", reachableMachines.length > 2);
    }

}
