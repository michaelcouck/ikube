package ikube.toolkit;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-09-2014
 */
public class MATRIXTest {

    @Test
    public void invertMatrix() {
        Object[][] matrix = {{1, 2, 3}, {2, 3, 4}, {3, 4, 5}};
        Object[][] invertedMatrix = MATRIX.invertMatrix(matrix);

        assertEquals(1, matrix[0][0]);
        assertEquals(3, matrix[1][1]);
        assertEquals(5, matrix[2][2]);

        assertEquals(3, invertedMatrix[0][0]);
        assertEquals(3, invertedMatrix[1][1]);
        assertEquals(3, invertedMatrix[2][2]);
    }

    @Test
    public void excludeColumns() {
        Object[][] data = new Object[][]{{1, 2, 3, 4}, {1, 2, 3, 4}, {1, 2, 3, 4}};
        Object[][] strippedData = MATRIX.excludeColumns(data, 2);
        assertEquals(3, strippedData.length);
        assertEquals(3, strippedData[0].length);
    }

    @Test
    public void sortOnFeature() {
        Object[][] matrix = {{3, 4, 5}, {2, 3, 4}, {1, 2, 3}};
        assertEquals(3, matrix[0][0]);
        assertEquals(3, matrix[1][1]);
        assertEquals(3, matrix[2][2]);

        MATRIX.sortOnFeature(matrix, 1, String.class);
        assertEquals(1, matrix[0][0]);
        assertEquals(3, matrix[1][1]);
        assertEquals(5, matrix[2][2]);

        matrix = new Object[][]{{1, 2, "2000-05-27"}, {2, 3, "1999-05-27"}, {3, 4, "1998-05-27"}};
        MATRIX.sortOnFeature(matrix, 2, Date.class);
        assertEquals(3, matrix[0][0]);
        assertEquals(3, matrix[1][1]);
        assertEquals("2000-05-27", matrix[2][2]);
    }

}