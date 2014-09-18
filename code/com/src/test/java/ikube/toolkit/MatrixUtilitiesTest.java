package ikube.toolkit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-09-2014
 */
public class MatrixUtilitiesTest {

    @Test
    public void excludeColumns() {
        Object[][] data = new Object[][]{{1, 2, 3, 4}, {1, 2, 3, 4}, {1, 2, 3, 4}};
        Object[][] strippedData = MatrixUtilities.excludeColumns(data, 2);
        assertEquals(3, strippedData.length);
        assertEquals(3, strippedData[0].length);
    }

}
