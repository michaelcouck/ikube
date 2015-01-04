package ikube.toolkit;

import ikube.AbstractTest;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 03-01-2015
 */
public class SORTTest extends AbstractTest {

    @Test
    public void sort() {
        int size = 10;
        int[] integers = new int[size];
        for (int i = integers.length - 1; i > 0; i--) {
            integers[i] = size - i;
        }
        SORT.sort(integers);
        int previous = 0;
        for (final int current : integers) {
            logger.error(previous + ":" + current);
            //noinspection StatementWithEmptyBody
            if (previous > 0) {
                // Assert.assertTrue(current > previous);
            }
            previous = current;
        }
    }

}
