package ikube.use;

import ikube.AbstractTest;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
public class ClickThroughTest extends AbstractTest {

    @Spy
    @InjectMocks
    private ClickThrough clickThrough;

    @Test
    public void hillClimb() throws Exception {
        clickThrough.hillClimb();
    }

}
