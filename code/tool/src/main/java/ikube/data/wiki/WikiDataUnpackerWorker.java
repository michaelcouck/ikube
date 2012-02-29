package ikube.data.wiki;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpackerWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerWorker.class);

	private StringBuilder stringBuilder;
	private static final String PAGE_START = "<revision>";
	private static final String PAGE_FINISH = "</revision>";

	private File directory;
	private int offset;

	/**
	 * Constructor sets up the variables like where to start reading the input stream and how much to read.
	 * 
	 * @param directory the output directory
	 * @param offset the offset in the stream to start reading from
	 * @param length the length of xml to read from the stream
	 */
	public WikiDataUnpackerWorker(final File directory, final int offset) {
		this.directory = directory;
		this.offset = offset;
	}

	public void initialize() {
		this.stringBuilder = new StringBuilder();
		// Seek to the offset in the input stream
		try {
			LOGGER.info("Skipping to offset : " + offset);
			// Skip to the last offset directory
			long directoryOffset = getDirectoryOffset();
			offset += directoryOffset;
			LOGGER.info("Skipped another : " + offset);
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
	}

	private long getDirectoryOffset() {
		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		long directoryOffset = 0;
		for (File file : files) {
			if (Long.parseLong(file.getName()) > directoryOffset) {
				directoryOffset = Long.parseLong(file.getName());
			}
		}
		return directoryOffset;
	}

	public void unpack(final byte[] bytes) throws Exception {
		int count = 0;
		File outputDirectory = null;
		String string = new String(bytes, 0, bytes.length, Charset.forName(IConstants.ENCODING));
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
			if (outputDirectory == null || count % 10000 == 0) {
				LOGGER.info("Count : " + count + ", position : " + offset);
				outputDirectory = FileUtilities.getFile(new File(directory, Long.toString(count)).getAbsolutePath(), Boolean.TRUE);
			}
			String filePath = outputDirectory.getAbsolutePath() + File.separator + hash + ".html";
			FileUtilities.setContents(filePath, segment.getBytes(Charset.forName(IConstants.ENCODING)));
			count++;
		}
	}

}