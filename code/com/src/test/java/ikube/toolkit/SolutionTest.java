package ikube.toolkit;

import org.junit.Test;

public class SolutionTest {

    @Test
    public void solutionMatrix() {
        int[][] matrix = {
                {5, 4, 4}, {4, 3, 4}, {3, 2, 4},
                {2, 2, 2}, {3, 3, 4}, {1, 4, 4},
                {4, 1, 1}, {5, 4, 4}, {4, 3, 4},
                {3, 2, 4}, {2, 2, 2}, {3, 3, 4},
                {1, 4, 4}, {4, 1, 1}};
        int countries = new Solution().solution(matrix);
        System.out.println(countries);
    }

    @Test
    public void solutionVector() throws InterruptedException {
        int[] matrix = {
                3, 4, 2, -1, -3, 1, 1, -3, -3, 2,
                -1, 4, -6, 2, -1, -3, 1, 1, -3, -3,
                2, 4, 2, -1, -3, 1, 1, -3, -3,
                2, -1, 4, -6, 2, -1, -3, 1, 1, -3};
        int notVisitable = new Solution().solution(matrix);
        System.out.println(matrix.length + "-" + notVisitable);
    }

}
