package ikube.search;

import ikube.AbstractTest;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 31-08-2014
 */
public class SearchLoadTest extends AbstractTest {

    @Test
    public void main() throws Exception {
        // We just execute this because there is nothing to test in the results
        SearchLoad.main(new String[]{"-i", "1000000", "-e", "1000", "-p", "8080", "-t", "10"});
    }

}
