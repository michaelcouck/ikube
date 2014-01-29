package ikube.toolkit;

public class Solution {

    public int solution(final int[][] matrix) {
        int countries = matrix.length * matrix[0].length;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i].length > j + 1) {
                    if (matrix[i][j] == matrix[i][j + 1]) {
                        countries--;
                    }
                }
                if (matrix.length > i + 1) {
                    if (matrix[i][j] == matrix[i + 1][j]) {
                        countries--;
                    }
                }
            }
        }
        return countries;
    }

    public int solution(final int[] A) throws InterruptedException {
        int offset = 0;
        int visited = 0;
        boolean[] flags = new boolean[A.length];
        do {
            visited++;
            flags[offset] = Boolean.TRUE;
            int previous = offset;
            offset += A[offset];
            if (offset == previous) {
                break;
            }
        } while (offset > 0 && offset < A.length && visited < A.length);
        int notVisited = flags.length;
        for (final boolean flag : flags) {
            if (flag) {
                notVisited--;
            }
        }
        return notVisited;
    }

}
