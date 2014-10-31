package ikube.action.index.content;

import java.io.ByteArrayOutputStream;

/**
 * This is just a wrapper class that allows access to the internal byte[].
 * 
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public class ByteOutputStream extends ByteArrayOutputStream {
	
	public ByteOutputStream() {
		super();
	}
	
	public ByteOutputStream(final byte[] buf) {
		super();
		this.buf = buf;
	}

	public byte[] getBytes() {
		return buf;
	}

	public int getCount() {
		return count;
	}
}