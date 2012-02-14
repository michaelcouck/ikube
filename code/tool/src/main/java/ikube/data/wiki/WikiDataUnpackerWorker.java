package ikube.data.wiki;

import java.io.InputStream;

public class WikiDataUnpackerWorker implements Runnable {

	private InputStream inputStream;
	private int offset;
	private int length;

	/**
	 * Constructor sets up the variables like where to start reading the input stream and how much to read.
	 * 
	 * @param inputStream the input stream to read the xml data from
	 * @param offset the offset in the stream to start reading from
	 * @param length the length of xml to read from the stream
	 */
	public WikiDataUnpackerWorker(final InputStream inputStream, final int offset, final int length) {
		this.inputStream = inputStream;
		this.offset = offset;
		this.length = length;
	}

	public void run() {
		// Seek to the offset in the input stream
		try {
			inputStream.skip(offset);
			byte[] bytes = new byte[1024];
			int read = -1;
			int count = 0;
			try {
				while ((read = inputStream.read()) > -1) {
					count += read;
					Thread.sleep(100);
					System.out.println("Thread : " + this.hashCode() + ", " + count + ", " + new String(bytes));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
