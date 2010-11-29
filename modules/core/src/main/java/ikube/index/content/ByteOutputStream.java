package ikube.index.content;

import java.io.ByteArrayOutputStream;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class ByteOutputStream extends ByteArrayOutputStream {

	public byte[] getBytes() {
		return buf;
	}

	public int getCount() {
		return count;
	}
}