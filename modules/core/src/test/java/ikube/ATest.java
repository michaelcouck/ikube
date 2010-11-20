package ikube;

import ikube.index.parse.mime.MimeMapper;
import ikube.index.parse.mime.MimeTypes;
import ikube.logging.Logging;

import org.apache.log4j.Logger;

public abstract class ATest {

	static {
		Logging.configure();
		new MimeTypes("/META-INF/mime/mime-types.xml");
		new MimeMapper("/META-INF/mime/mime-mapping.xml");
	}

	protected Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Returns the max read length byte array plus 1000, i.e. more than the max bytes that the application can read. This forces the indexer
	 * to get a reader rather than a string.
	 *
	 * @param string
	 *            the string to copy to the byte array until the max read length is exceeded
	 * @return the byte array of the string copied several times more than the max read length
	 */
	protected byte[] getBytes(String string) {
		byte[] bytes = new byte[(int) (IConstants.MAX_READ_LENGTH + IConstants.MAX_READ_LENGTH + 1000)];
		for (int offset = 0; offset < bytes.length;) {
			byte[] segment = string.getBytes();
			if (offset + segment.length >= bytes.length) {
				break;
			}
			System.arraycopy(segment, 0, bytes, offset, segment.length);
			offset += segment.length;
		}
		return bytes;
	}

}
