package ikube.toolkit;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public final class HashUtilities {
	
	private HashUtilities() {
	}

	/**
	 * Simple, fast hash function to generate quite unique hashes from strings(i.e. toCharArray()).
	 *
	 * @param string
	 *            the string to generate the hash from
	 * @return the integer representation of the hash of the string characters, typically quite unique for strings less than 10 characters
	 */
	public static Long hash(final String string) {
		// Must be prime of course
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			hash = (hash * seed) + chars[i];
		}
		return Long.valueOf(Math.abs(hash));
	}

	/**
	 * Builds a hash from an array of objects.
	 *
	 * @param objects
	 *            the objects to build the hash from
	 * @return the hash of the objects
	 */
	public static final Long hash(final Object... objects) {
		StringBuilder builder = new StringBuilder();
		for (Object object : objects) {
			builder.append(object);
		}
		return HashUtilities.hash(builder.toString());
	}

}
