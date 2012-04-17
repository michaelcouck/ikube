package ikube.data.wiki;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will read a Bzip2 file with a big xml in it, unpack the xml, parse it and write individual files to disk.
 * 
 * @author Michael Couck
 * @since at least 14.04.2012
 * @version 01.00
 */
public class WikiDataUnpackerWorker implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerWorker.class);

	/** This is the start and end tags for the xml data, one per page essentially. */
	private static final String PAGE_START = "<revision>";
	private static final String PAGE_FINISH = "</revision>";

	/** The input Bzip2 xml file. */
	private File file;
	/** The disk where this file is to be unpacked. */
	private File disk;

	/**
	 * Constructor takes the input file, i.e. the Bzip file with the xml data, and the disk where the file is to be unpacked.
	 * 
	 * @param file the input file
	 * @param disk the output disk
	 */
	public WikiDataUnpackerWorker(final File file, final File disk) {
		this.file = file;
		this.disk = disk;
	}

	/**
	 * This method will read the zip file, add the contents to the string builder then write it out to the file.
	 */
	@Override
	public void run() {
		FileInputStream fileInputStream = null;
		BZip2CompressorInputStream bZip2CompressorInputStream = null;
		try {
			int read = -1;
			int count = 0;
			File outputDirectory = getNextDirectory();
			fileInputStream = new FileInputStream(file);
			bZip2CompressorInputStream = new BZip2CompressorInputStream(fileInputStream);
			byte[] bytes = new byte[1024 * 1024];
			StringBuilder stringBuilder = new StringBuilder();
			while ((read = bZip2CompressorInputStream.read(bytes)) > -1) {
				String string = new String(bytes, 0, read, Charset.forName(IConstants.ENCODING));
				stringBuilder.append(string);
				count += unpack(outputDirectory, stringBuilder);
				if (count > 10000) {
					outputDirectory = getNextDirectory();
					LOGGER.info("Count : " + count + ", output directory : " + outputDirectory);
					count = 0;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception reading and uncompressing the zip file : " + file + ", " + disk, e);
		} finally {
			FileUtilities.close(fileInputStream);
			FileUtilities.close(bZip2CompressorInputStream);
		}
	}

	/**
	 * This method will take the input data(the string builder) and parse it, breaking the xml into 'pages' between the start and end tags,
	 * then write this 'segment' to individual files.
	 * 
	 * @param outputDirectory the directory where to write the output files
	 * @param stringBuilder the string of xml data to parse and write
	 * @return the number of files written to the disk
	 * @throws Exception
	 */
	private int unpack(final File outputDirectory, final StringBuilder stringBuilder) throws Exception {
		int count = 0;
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
			String filePath = outputDirectory.getAbsolutePath() + File.separator + hash + ".html";
			FileUtilities.setContents(filePath, segment.getBytes(Charset.forName(IConstants.ENCODING)));
			count++;
			if (count % 1000 == 0) {
				LOGGER.info("Count : " + count + ", " + this.hashCode());
			}
		}
		return count;
	}

	private File getNextDirectory() {
		String path = disk.getAbsolutePath() + File.separator + Long.toString(System.currentTimeMillis());
		File outputDirectory = FileUtilities.getFile(path, Boolean.TRUE);
		return outputDirectory;
	}

}