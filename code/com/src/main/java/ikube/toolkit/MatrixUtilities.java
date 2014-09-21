package ikube.toolkit;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * @return the double array from the input
     */
    public static double[] objectVectorToDoubleVector(final Object[] vector) {
        double[] doubleVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
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
     * @return the string array from the input
     */
    public static String[] objectVectorToStringVector(final Object[] vector) {
        String[] stringVector = new String[vector.length];
        for (int i = 0; i < vector.length; i++) {
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

    /**
     * This method will take a matrix, and based on the length of the matrix create a permutation of
     * columns that can be excluded from the matrix to reduce the vector space. This could be useful when
     * hill climbing and trying to remove the 'noisy' features from a data set. For example the matrix is:
     * <p/>
     * <pre>
     *     [[1,2,3,4]
     *      [4,5,6,7]
     *      [7,8,9,10]]
     * </pre>
     * <p/>
     * Calling this method with a 35% excluded columns will result in a matrix of:
     * <p/>
     * <pre>
     *     [[2]
     *      [2,3]
     *      [3]]
     * </pre>
     *
     * @param matrix                    the initial matrix to try to reduce by a certain feature set
     * @param excludedColumnsPercentage the percentage of the matrix to use to reduce the features
     * @return the matrix of columns that can be used to reduce the vector space of the input matrix. Note that
     * the features are removed from the end of the vectors, not the begining
     */
    public static int[][] excludedColumnsPermutation(final Object[][] matrix, final int excludedColumnsPercentage) {
        List<int[]> excludedColumnsList = new ArrayList<>();
        int lengthOfExcludedColumnsArray = matrix[0].length * excludedColumnsPercentage / 100;
        // Create an initial vector/set
        ICombinatoricsVector<Integer> initialSet = Factory.createVector();
        for (int i = 0; i < lengthOfExcludedColumnsArray; i++) {
            initialSet.addValue(matrix[0].length - lengthOfExcludedColumnsArray + i);
        }
        // Create an instance of the subset generator
        Generator<Integer> generator = Factory.createSubSetGenerator(initialSet);
        // Print the subsets
        for (ICombinatoricsVector<Integer> subSet : generator) {
            int[] subExcludedColumns = new int[subSet.getSize()];
            for (int i = 0; i < subSet.getSize(); i++) {
                subExcludedColumns[i] = subSet.getValue(i);
            }
            excludedColumnsList.add(subExcludedColumns);
        }
        int[][] excludedColumnsArray = new int[excludedColumnsList.size()][];
        for (int i = 0; i < excludedColumnsArray.length; i++) {
            excludedColumnsArray[i] = excludedColumnsList.get(i);
        }
        return excludedColumnsArray;
    }

}