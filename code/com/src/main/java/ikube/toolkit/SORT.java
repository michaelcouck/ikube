package ikube.toolkit;

import java.util.Arrays;

/**
 * Fiddling with sorting...
 *
 * @author Michael Couck
 * @version 01.00
 * @since 03-01-2015
 */
public class SORT {

    public static void sort(final int[] integers) {
        int i = 0;
        while (true) {
            if (i + 1 >= integers.length) {
                i--;
            }
            if (i < 0) {
                break;
            }
            System.out.println(i);
            System.out.println(Arrays.toString(integers));
            if (integers[i] > integers[i + 1]) {
                swap(i, i + 1, integers);
                i++;
            } else {
                i--;
            }
            if (i == 0 && integers[i] < integers[i + 1]) {
                break;
            }
        }
    }

    private static void swap(final int i, final int j, final int[] integers) {
        int a = integers[i];
        integers[i] = integers[j];
        integers[j] = a;
    }

}
