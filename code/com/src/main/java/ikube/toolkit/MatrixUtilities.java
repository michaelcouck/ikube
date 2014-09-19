package ikube.toolkit;

import java.util.Arrays;

/**
 * General matrix functions that are not available in open source libraries.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 16-09-2014
 */
public final class MatrixUtilities {

    /**
     * Inverts a matrix from an m-n to an n-m matrix.
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
     * Converts an object vector to a double[], with the specified length.
     *
     * @param vector the array to convert to the double array
     * @param length the maximum length of the array
     * @return the double array from the input
     */
    public static double[] objectVectorToDoubleVector(final Object[] vector, final int length) {
        double[] doubleVector = new double[length];
        for (int i = 0; i < vector.length && i < doubleVector.length; i++) {
            String element = vector[i].toString();
            if (StringUtilities.isNumeric(element)) {
                doubleVector[i] = Double.parseDouble(element);
            } else {
                doubleVector[i] = HashUtilities.hash(element);
            }
        }
        return doubleVector;
    }

    /**
     * Converts an object vector to a String[], with the specified length.
     *
     * @param vector the array to convert to the string array
     * @param length the maximum length of the array
     * @return the string array from the input
     */
    public static String[] objectVectorToStringVector(final Object[] vector, final int length) {
        String[] stringVector = new String[length];
        for (int i = 0; i < vector.length && i < stringVector.length; i++) {
            if (String.class.isAssignableFrom(vector[i].getClass())) {
                stringVector[i] = (String) vector[i];
            } else {
                stringVector[i] = vector[i].toString();
            }
        }
        return stringVector;
    }

    /**
     * This method will create a matrix as specified, but removing the columns specified
     * by the excluded parameter. Hence the matrix will be data[0].length - excludedColumns.length
     *
     * @param matrix          the matrix to exclude the columns from
     * @param excludedColumns the indexes of the columns in the matrix to exclude
     * @return the matrix stripped of it's columns, note it is a new object reference
     */
    public static Object[][] excludeColumns(final Object[][] matrix, final int... excludedColumns) {
        Object[][] prunedMatrix = new Object[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            Object[] vector = matrix[i];
            Object[] prunedVector = new Object[vector.length - excludedColumns.length];
            for (int j = 0, k = 0; j < vector.length; j++) {
                if (Arrays.binarySearch(excludedColumns, j) < 0 && prunedVector.length > k && vector.length > j) {
                    prunedVector[k++] = vector[j];
                }
            }
            prunedMatrix[i] = prunedVector;
        }
        return prunedMatrix;
    }

}