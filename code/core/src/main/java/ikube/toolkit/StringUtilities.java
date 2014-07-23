package ikube.toolkit;

import org.apache.commons.lang.StringUtils;

/**
 * General utilities for strings that are not available in the general string classes from Apache or Spring.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10.01.2012
 */
public final class StringUtilities {

	private static final char SPACE = ' ';

	/**
	 * This method will check to see if a string can be parsed into a number of sorts,
	 * like a double for example. If there is a separator like a '.' or a ','
	 * then this will still qualify as a number, but more than one dot foe example then dis-qualifies the string as a
	 * number.
	 * <p/>
	 * <pre>
	 * 1) 123 - true
	 * 2) 123.456 - true
	 * 3) 123.456.789 - false
	 * 4) 123.456,789 - false
	 * 5) . - false
	 * 6) 1. - false
	 * 7) .1 - false
	 * </pre>
	 * <p/>
	 * This method is faster that the string utilities from Apache, as a matter of interest.
	 *
	 * @param string the string to check whether it qualifies to be parsed into a number, could be a double
	 * @return whether the string has only numbers and potentially one separator a comma or a dot, but no spaces
	 */
	@SuppressWarnings("ConstantConditions")
    public static boolean isNumeric(final String string) {
		if (string == null) {
			return Boolean.FALSE;
		}
		final char[] chars = string.toCharArray();
		boolean before = Boolean.FALSE;
		boolean dot = Boolean.FALSE;
		boolean after = Boolean.FALSE;
		for (final char c : chars) {
			if (Character.isDigit(c)) {
				if (!dot) {
					before = Boolean.TRUE;
				} else {
					after = Boolean.TRUE;
				}
			} else if ('.' == c || ',' == c) {
				if (dot) {
					// If we find more than one dot or number separator
					// then this is not a number per se but some kind of string
					// identifier like 123.456.789, which is not numeric
					return Boolean.FALSE;
				}
				dot = Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}
		// There must be at least one number in the string, and of there is a dot
		// then there must be a number before the dot and one after the dot/comma
		return (before && !dot) || (before && dot && after);
	}

	public static String strip(final String string, final String strip) {
		char[] chars = string.toCharArray();
		char[] strippedChars = new char[chars.length];
		char[] stripChars = strip.toCharArray();
		int j = 0;
		for (final char c : chars) {
			boolean equals = Boolean.FALSE;
			for (char stripChar : stripChars) {
				if (c == stripChar) {
					equals = Boolean.TRUE;
					break;
				}
			}
			if (!equals) {
				strippedChars[j++] = c;
			} else {
				if (j == 0) {
					strippedChars[j++] = SPACE;
				} else {
					if (strippedChars[j - 1] != SPACE) {
						strippedChars[j++] = SPACE;
					}
				}
			}
		}
		return new String(strippedChars, 0, j);
	}

	/**
	 * Remove single characters and numbers and anything that isn't human
	 * and strips the whitespace to one character if there are more than one. Will
     * also remove all punctuation.
	 *
	 * @param content the string to strip non-human readable characters from
	 * @return the cleaned string, all alpha numeric characters
	 */
	public static String stripToAlphaNumeric(final String content) {
		if (!StringUtils.isEmpty(content)) {
			StringBuilder b = new StringBuilder();
			char p = SPACE;
			char[] cs = content.toCharArray();
			for (final char c : cs) {
				boolean a = Boolean.FALSE;
				if (Character.isWhitespace(c)) {
					if (p != SPACE) {
						a = Boolean.TRUE;
					}
				} else if (Character.isAlphabetic(c)) {
					a = Boolean.TRUE;
				} else {
					if (p != ' ') {
						p = SPACE;
						b.append(SPACE);
					}
				}
				if (a) {
					p = c;
					b.append(c);
				}
			}
			return b.toString().toLowerCase();
		}
		return content;
	}

}