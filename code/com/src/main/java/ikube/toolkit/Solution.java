package ikube.toolkit;

public class Solution {

    public int solution(final int[][] A) {
        int countries = A.length * A[0].length;
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                if (A[i].length > j + 1) {
                    if (A[i][j] == A[i][j + 1]) {
                        countries--;
                    }
                }
                if (A.length > i + 1) {
                    if (A[i][j] == A[i + 1][j]) {
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