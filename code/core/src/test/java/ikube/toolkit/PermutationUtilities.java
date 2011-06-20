package ikube.toolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class generates the permutations for parameters.
 *
 * @author Michael Couck
 * @since 06.10.09
 * @version 01.00
 */
public class PermutationUtilities {

	protected Logger logger = Logger.getLogger(PermutationUtilities.class);

	public void getPermutations(Object[] objects) {
		int N = objects.length;
		Object[] newObjects = new Object[N];
		for (int i = 0; i < N; i++) {
			newObjects[i] = objects[i];
		}
		List<Object[]> permutations = new ArrayList<Object[]>();
		getPermutations(newObjects, permutations, N, 0);
	}

  /**
     * This method will get permutations for a certain array of objects. For example if there is an
     * array of three objects, new Object[] {one, two, three}, then all the possible permutations
     * for the array are:
     * 
     * <pre>
     *  1) [two, three, one]
     *  2) [three, two, one]
     *  3) [three, one, two]
     *  4) [one, three, two]
     *  5) [two, one, three]
     *  6) [one, two, three]
     * </pre>
     * 
     * Note that the number of permutations increases exponentially with the increase in the number
     * of objects.
     * 
     * @param <T> t the type
     * @param objects the objects to get the permutation for
     * @param permutations some permutations
     * @param numberOfObjects some value
     * @param counter the counter you idiot
     */
	public <T> void getPermutations(T[] objects, List<T[]> permutations, int n, int counter) {
		if (n == 1) {
			permutations.add(Arrays.copyOf(objects, objects.length));
			return;
		}
		for (int i = 0; i < n; i++) {
			swap(objects, i, n - 1);
			getPermutations(objects, permutations, n - 1, ++counter);
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