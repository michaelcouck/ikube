package ikube;

import ikube.toolkit.Logging;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18.06.13
 */
@Ignore
public abstract class AbstractTest {

    static {
        Logging.configure();
    }

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

}