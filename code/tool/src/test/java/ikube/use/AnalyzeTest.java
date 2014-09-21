package ikube.use;

import ikube.AbstractTest;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
@Ignore
public class AnalyzeTest extends AbstractTest {

    @Test
    public void analyze() throws Exception {
        String[] args = {
                "-n", "click-through-0-1-100000",
                "-t", "test-1000.csv",
                "-f", "3"
        };
        Analyze.main(args);
    }

    @Test
    public void bitShift() {
        System.out.println(" " + (1 | -1));
        System.out.println(" " + (1 | 1));
        System.out.println(" " + (1 | 0));
        System.out.println(" " + (1 << 1));

    }

}
