package ikube.search;

import ikube.AbstractTest;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-02-2014
 */
public class SearchToolkitTest extends AbstractTest {

    @Test
    @Ignore
    public void main() {
        String[] args = {"/mnt/sdb/indexes/indexContext/1392974240185/127.0.1.1/", "id", "eacbs"};
        SearchToolkit.main(args);
    }

}
