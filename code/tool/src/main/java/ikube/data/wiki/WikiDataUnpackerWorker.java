package ikube.data.wiki;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpackerWorker implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerWorker.class);

	private static final int BUFFER = 2048;
	private static final String PAGE_START = "<page>";
	private static final String PAGE_FINISH = "</page>";

	private InputStream inputStream;
	private int offset;
	private int length;
	private File directory;

	/**
	 * Constructor sets up the variables like where to start reading the input stream and how much to read.
	 * 
	 * @param inputStream the input stream to read the xml data from
	 * @param offset the offset in the stream to start reading from
	 * @param length the length of xml to read from the stream
	 */
	public WikiDataUnpackerWorker(final InputStream inputStream, final int offset, final int length, final File directory) {
		this.inputStream = inputStream;
		this.offset = offset;
		this.length = length;
		this.directory = directory;
	}

	public void run() {
		// Seek to the offset in the input stream
		try {
			LOGGER.info("Skipping to offset : " + offset);
			inputStream.skip(offset);
			// Skip to the last offset directory
			long directoryOffset = getDirectoryOffset();
			inputStream.skip(directoryOffset);
			LOGGER.info("Skipped another : " + directoryOffset);
			// Unpack the xml file into single files
			unpack(offset + directoryOffset);
		} catch (Exception e) {
			e.printStackTrace();
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

	private void unpack(final long initialOffset) throws Exception {
		int read = -1;
		int count = 0;
		long offset = initialOffset;
		File outputDirectory = null;
		byte[] bytes = new byte[1024 * 1024];
		StringBuilder stringBuilder = new StringBuilder();
		while ((read = inputStream.read(bytes)) > -1 && offset < length) {
			offset += read;
			Thread.sleep(100);
			String string = new String(bytes, 0, read, Charset.forName(IConstants.ENCODING));
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
					if (outputDirectory != null) {
						// Now zip the files to compress them
						// pack(outputDirectory);
					}
					LOGGER.info("Count : " + count + ", position : " + offset);
					outputDirectory = new File(directory, Long.toString(count));
				}
				String filePath = outputDirectory.getAbsolutePath() + File.separator + hash + ".html";
				FileUtilities.setContents(filePath, segment.getBytes(Charset.forName(IConstants.ENCODING)));
				count++;
			}
		}
	}

	protected void pack(File outputDirectory) {
		File zipFile = new File(outputDirectory, outputDirectory.getName() + ".zip");
		zipFile = FileUtilities.getFile(zipFile.getAbsolutePath(), Boolean.FALSE);

		FileOutputStream zipFileOutputStream = null;
		ZipOutputStream zipOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		try {
			zipFileOutputStream = new FileOutputStream(zipFile);
			bufferedOutputStream = new BufferedOutputStream(zipFileOutputStream);
			zipOutputStream = new ZipOutputStream(bufferedOutputStream);
			byte data[] = new byte[BUFFER];
			File[] files = outputDirectory.listFiles();
			for (int i = 0; i < files.length; i++) {
				FileInputStream fileInputStream = null;
				BufferedInputStream bufferedInputStream = null;
				try {
					LOGGER.info("Adding: " + files[i]);
					fileInputStream = new FileInputStream(files[i]);
					bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER);
					ZipEntry entry = new ZipEntry(files[i].getName());
					zipOutputStream.putNextEntry(entry);
					int count;
					while ((count = bufferedInputStream.read(data, 0, BUFFER)) != -1) {
						zipOutputStream.write(data, 0, count);
					}
				} catch (Exception e) {
					LOGGER.error(null, e);
				} finally {
					FileUtilities.close(fileInputStream);
					FileUtilities.close(bufferedInputStream);
					// And delete the source if we can
					boolean deleted = FileUtilities.deleteFile(files[i], 1);
					if (!deleted) {
						LOGGER.warn("Couldn't delete file : " + files[i]);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(null, e);
		} finally {
			FileUtilities.close(zipOutputStream);
		}
	}

}