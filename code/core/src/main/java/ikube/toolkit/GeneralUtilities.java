package ikube.toolkit;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.util.ReflectionUtils;

/**
 * @author Michael Couck
 * @since 27.02.11
 * @version 01.00
 */
public final class GeneralUtilities {

	private static final Logger LOGGER = Logger.getLogger(GeneralUtilities.class);

	private GeneralUtilities() {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void quickSort(final Comparable[] comparables, final int start, final int end) {
		if (end <= start) {
			return;
		}
		Comparable middle = comparables[start];
		int i = start;
		int j = end + 1;
		for (;;) {
			do {
				i++;
			} while (i < end && comparables[i].compareTo(middle) < 0);
			do {
				j--;
			} while (j > start && comparables[j].compareTo(middle) > 0);
			if (j <= i) {
				break;
			}
			Comparable smaller = comparables[i];
			Comparable larger = comparables[j];
			comparables[i] = larger;
			comparables[j] = smaller;
		}
		comparables[start] = comparables[j];
		comparables[j] = middle;
		quickSort(comparables, start, j - 1);
		quickSort(comparables, j + 1, end);
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

	public static void main(final String[] args) throws Exception {
		Integer[] arr = new Integer[5];
		LOGGER.info("inserting: ");
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Integer.valueOf((int) (Math.random() * 99));
			LOGGER.info(arr[i] + " ");
		}
		quickSort(arr, 0, arr.length - 1);
		LOGGER.info("\nsorted: ");
		for (int i = 0; i < arr.length; i++) {
			LOGGER.info(arr[i] + " ");
		}
		LOGGER.info("\nDone ;-)");
	}

	/**
	 * Finds an open port.
	 * 
	 * @param port
	 *            the port number to start from
	 * @return the first available port from the starting port
	 */
	public static synchronized int findFirstOpenPort(int port) {
		try {
			ServerSocket ss = null;
			DatagramSocket ds = null;
			while (true && port < Short.MAX_VALUE) {
				try {
					ss = new ServerSocket(port);
					ss.setReuseAddress(true);
					ds = new DatagramSocket(port);
					ds.setReuseAddress(true);
					return port;
				} catch (IOException e) {
					port++;
				} finally {
					if (ds != null) {
						ds.close();
					}
					if (ss != null) {
						try {
							ss.close();
						} catch (Exception e) {
							// Should not be thrown
						}
					}
				}
			}
			throw new RuntimeException("Couldn't find an open port : " + port);
		} finally {
			GeneralUtilities.class.notifyAll();
		}
	}

	public static <T> T findObject(Class<T> klass, Collection<T> collection, String fieldName, String fieldValue) {
		for (T t : collection) {
			Field field = ReflectionUtils.findField(t.getClass(), fieldName);
			if (field != null) {
				field.setAccessible(Boolean.TRUE);
				Object value = ReflectionUtils.getField(field, t);
				if (fieldValue != null) {
					if (fieldValue.equals(value)) {
						return t;
					}
				}
			}
		}
		return null;
	}

}
