package ikube.toolkit;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class generates the permutations for parameters.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 06-10-2009
 */
public class PERMUTAION {

    protected Logger logger = Logger.getLogger(PERMUTAION.class);

    /**
     * @param objects
     */
    public void getPermutations(Object[] objects) {
        int N = objects.length;
        Object[] newObjects = new Object[N];
        System.arraycopy(objects, 0, newObjects, 0, N);
        List<Object[]> permutations = new ArrayList<>();
        getPermutations(newObjects, permutations, N, 0);
    }

    /**
     * This method will get permutations for a certain array of objects. For example if there is an
     * array of three objects, new Object[] {one, two, three}, then all the possible permutations
     * for the array are:
     * <p/>
     * <pre>
     *  1) [two, three, one]
     *  2) [three, two, one]
     *  3) [three, one, two]
     *  4) [one, three, two]
     *  5) [two, one, three]
     *  6) [one, two, three]
     * </pre>
     * <p/>
     * Note that the number of permutations increases exponentially with the increase in the number
     * of objects.
     *
     * @param <T>          t the type
     * @param objects      the objects to get the permutation for
     * @param permutations some permutations
     * @param n            some value
     * @param counter      the counter you idiot
     */
    public <T> void getPermutations(T[] objects, List<T[]> permutations, int n, int counter) {
        if (n == 1) {
            permutations.add(Arrays.copyOf(objects, objects.length));
            return;
        }
        for (int i = 0; i < n; i++) {
            swap(objects, i, n - 1);
            getPermutations(objects, permutations, n - 1, counter + 1);
            swap(objects, i, n - 1);
        }
    }

    private Object[] swap(Object[] objects, int i, int j) {
        Object s = objects[i];
        objects[i] = objects[j];
        objects[j] = s;
        return objects;
    }

}