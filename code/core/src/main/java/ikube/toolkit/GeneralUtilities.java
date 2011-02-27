package ikube.toolkit;

/**
 * @author Michael Couck
 * @since 27.02.11
 * @version 01.00
 */
public class GeneralUtilities {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void quickSort(Comparable[] c, int start, int end) {
		if (end <= start) {
			return;
		}
		Comparable middle = c[start];
		int i = start;
		int j = end + 1;
		for (;;) {
			do {
				i++;
			} while (i < end && c[i].compareTo(middle) < 0);
			do {
				j--;
			} while (j > start && c[j].compareTo(middle) > 0);
			if (j <= i) {
				break;
			}
			Comparable smaller = c[i];
			Comparable larger = c[j];
			c[i] = larger;
			c[j] = smaller;
		}
		c[start] = c[j];
		c[j] = middle;
		quickSort(c, start, j - 1);
		quickSort(c, j + 1, end);
	}

	public static void main(String[] args) throws Exception {
		Integer[] arr = new Integer[5];
		System.out.println("inserting: ");
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new Integer((int) (Math.random() * 99));
			System.out.print(arr[i] + " ");
		}
		quickSort(arr, 0, arr.length - 1);
		System.out.println("\nsorted: ");
		for (int i = 0; i < arr.length; i++)
			System.out.print(arr[i] + " ");
		System.out.println("\nDone ;-)");
	}

}
