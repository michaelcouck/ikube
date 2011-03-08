package ikube.toolkit;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 27.02.11
 * @version 01.00
 */
public class GeneralUtilities {
	
	private static final Logger LOGGER = Logger.getLogger(GeneralUtilities.class);
	
	private GeneralUtilities() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void quickSort(final Comparable[] c, final int start, final int end) {
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

	/**
	 * This method calculates the distance on the earth between two points in kilometers.
	 * 
	 * @param lat1
	 *            the latitude of the first point
	 * @param lng1
	 *            the longitude of the first point
	 * @param lat2
	 *            the latitude of the second point
	 * @param lng2
	 *            the longitude of the second point
	 * @return the distance between the two points
	 */
	public static float distFrom(final float lat1, final float lng1, final float lat2, final float lng2) {
		double earthRadius = 6378.1;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;
		return new Float(dist).floatValue();
	}

	public static void main(String[] args) throws Exception {
		Integer[] arr = new Integer[5];
		LOGGER.info("inserting: ");
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new Integer((int) (Math.random() * 99));
			LOGGER.info(arr[i] + " ");
		}
		quickSort(arr, 0, arr.length - 1);
		LOGGER.info("\nsorted: ");
		for (int i = 0; i < arr.length; i++) {
			LOGGER.info(arr[i] + " ");
		}
		LOGGER.info("\nDone ;-)");
	}

}
