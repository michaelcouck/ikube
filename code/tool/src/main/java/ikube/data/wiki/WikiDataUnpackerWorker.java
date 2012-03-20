package ikube.data.wiki;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.File;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpackerWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerWorker.class);

	private StringBuilder stringBuilder;
	private static final String PAGE_START = "<revision>";
	private static final String PAGE_FINISH = "</revision>";

	private int count;
	private File directory;

	/**
	 * Constructor sets up the variables like where to start reading the input stream and how much to read.
	 * 
	 * @param directory the output directory
	 */
	public WikiDataUnpackerWorker() {
		this.stringBuilder = new StringBuilder();
	}

	public int unpack(final byte[] bytes, int start, int length) throws Exception {
		String string = new String(bytes, start, length, Charset.forName(IConstants.ENCODING));
		stringBuilder.append(string);
		while (true) {
			int startOffset = stringBuilder.indexOf(PAGE_START);
			int endOffset = stringBuilder.indexOf(PAGE_FINISH);
			if (startOffset == -1 || endOffset == -1) {
				break;
			}
			if (endOffset <= startOffset) {
				startOffset = endOffset;
			}
			endOffset += PAGE_FINISH.length();
			String segment = stringBuilder.substring(startOffset, endOffset);
			stringBuilder.delete(startOffset, endOffset);
			String hash = Long.toString(HashUtilities.hash(segment));
			if (count % 10000 == 0) {
				LOGGER.info("Count : " + count);
			}
			String filePath = directory.getAbsolutePath() + File.separator + hash + ".html";
			FileUtilities.setContents(filePath, segment.getBytes(Charset.forName(IConstants.ENCODING)));
			count++;
		}
		return count;
	}

	protected void setDirectory(final File directory) {
		this.directory = directory;
	}

}