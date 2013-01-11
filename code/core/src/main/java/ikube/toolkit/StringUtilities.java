package ikube.toolkit;

public class StringUtilities {

	public static final boolean isNumeric(final String string) {
		char[] chars = string.toCharArray();
		for (char c : chars) {
			if (Character.isDigit(c)) {
				continue;
			} else if ('.' == c) {
				continue;
			} else {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

}
