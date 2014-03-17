package ikube.web;

import ikube.BaseTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2014
 */
@Ignore
public class IkubeIntegration extends BaseTest {

    @Test
    public void start() {
        String[] parameters = {"start", Integer.toString(SERVER_PORT + 1)};
        Ikube.main(parameters);
        assertTrue(Ikube.SERVER.isRunning());
    }

    @Test
    public void stop() {
        start();
        String[] parameters = {"stop"};
        Ikube.main(parameters);
        assertFalse(Ikube.SERVER.isRunning());
    }

}