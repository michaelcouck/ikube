package ikube.web.service;

import ikube.AbstractTest;
import ikube.toolkit.REST;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Michael couck
 * @version 01.00
 * @since 06-04-2015
 */
public class MonitorIntegration extends AbstractTest {

    @Test
    public void terminateAll() throws Exception {
        String url = getUrl(Monitor.MONITOR + Monitor.TERMINATE_ALL);
        Object result = REST.doPost(url, null, String.class);
        logger.info("Result : " + result);
        assertNotNull(result);
    }

}