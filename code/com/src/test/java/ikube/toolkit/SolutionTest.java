package ikube.toolkit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SolutionTest {

    @Test
    public void solutionMatrix() {
        int[][] matrix = {
                {5, 4, 4}, {4, 3, 4}, {3, 2, 4},
                {2, 2, 2}, {3, 3, 4}, {1, 4, 4},
                {4, 1, 1}};
        int countries = new Solution().solution(matrix);
        assertEquals(11, countries);
    }

    @Test
    public void solutionVector() throws InterruptedException {
        int notVisitable;
        int[] matrix = {
                3, 4, 2, -1, -3, 1, 1, -3, -3, 2,
                -1, 4, -6, 2, -1, -3, 1, 1, -3, -3,
                2, 4, 2, -1, -3, 1, 1, -3, -3,
                2, -1, 4, -6, 2, -1, -3, 1, 1, -3};
        notVisitable = new Solution().solution(matrix);
        assertEquals(31, notVisitable);

        matrix = new int[]{3, -5, 0, -1, -3};
        notVisitable = new Solution().solution(matrix);

        assertEquals(2, notVisitable);
    }

}