package ikube.action.index.parse;

import ikube.IConstants;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * TODO Re-implement this class using system array copies.
 * 
 * @author Michael Couck
 * @since 03.09.10
 * @version 01.00
 */
public class TextParser implements IParser {

	private static final char SPACE = ' ';

	private static final int LOW_ARABIC = 1611;
	private static final int HIGH_ARABIC = 1621;
	private static final char NEW_LINE = '\n';
	private static final char CARRIAGE_RETURN = '\r';
	private static final char[] CHARS = new char[1024];

	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		StringBuilder builder = new StringBuilder();
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream, IConstants.ENCODING);
		int read = inputStreamReader.read(CHARS);
		while (read > -1) {
			char previous = 0;
			for (int i = 0; i < read; i++) {
				char c = CHARS[i];
				if (Character.isLetterOrDigit(c)) {
					builder.append(c);
					previous = c;
				} else if (Character.isSpaceChar(c)) {
					if (previous != SPACE) {
						builder.append(c);
					}
					previous = c;
				} else if (c == NEW_LINE || c == CARRIAGE_RETURN) {
					builder.append(SPACE);
					previous = SPACE;
				} else if (previous != SPACE) {
					// Test for the vowel Arabic character
					if (c > LOW_ARABIC && c < HIGH_ARABIC) {
						builder.append(c);
						previous = c;
					} else {
						builder.append(SPACE);
						previous = SPACE;
					}
				}
			}
			read = inputStreamReader.read(CHARS);
		}
		outputStream.write(builder.toString().getBytes());
		return outputStream;
	}

}
