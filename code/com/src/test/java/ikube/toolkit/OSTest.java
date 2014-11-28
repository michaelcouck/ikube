package ikube.toolkit;

import ikube.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 28-03-2014
 */
public class OSTest extends AbstractTest {

    @Test
    public void isOs() {
        boolean isOs = OS.isOs("don't know what os we are on");
        assertFalse(isOs);
    }

}
