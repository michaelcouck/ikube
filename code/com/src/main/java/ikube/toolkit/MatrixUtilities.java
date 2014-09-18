package ikube.toolkit;

/**
 * General matrix functions that are not available in open source libraries.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 16-09-2014
 */
public final class MatrixUtilities {

    /**
     * Inverts a patrix from an m-n to an n-m matrix.
     *
     * @param matrix the matrix to invert
     * @return the inverted matrix, from m-n to n-m
     */
    public static Object[][] invertMatrix(final Object[][] matrix) {
        final int m = matrix.length;
        final int n = matrix[0].length;
        Object[][] inverted = new Object[n][m];
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                inverted[c][m - 1 - r] = matrix[r][c];
            }
        }
        return inverted;
    }

    /**
     * Converts an object array to a double[], with the specified length.
     *
     * @param array  the array to convert to the double array
     * @param length the maximum length of the array
     * @return the double array from the input
     */
    public static double[] objectArrayToDoubleArray(final Object[] array, final int length) {
        double[] doubleArray = new double[length];
        for (int i = 0; i < array.length && i < doubleArray.length; i++) {
            doubleArray[i] = Double.parseDouble(array[i].toString());
        }
        return doubleArray;
    }

}