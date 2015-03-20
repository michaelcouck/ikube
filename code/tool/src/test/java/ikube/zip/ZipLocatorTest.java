package ikube.zip;

import ikube.AbstractTest;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-02-2011
 */
public class ZipLocatorTest extends AbstractTest {

    @Test
    public void main() {
        String[] args = {".", ".*(jar.jar).*", "OrderBy", "OrderBy"};
        ZipLocator.main(args);
        assertTrue(ZipLocator.ATOMIC_INTEGER.get() > 0);
    }

    @Test
    @Ignore
    public void adHoc() {
        // String[] args = {"/opt", ".*(.jar)\\Z", "SLF4JLocationAwareLog"};
        String[] args = {"/opt", ".*(.jar)\\Z", "JDK14LoggerFactory"};
        ZipLocator.main(args);
    }

}