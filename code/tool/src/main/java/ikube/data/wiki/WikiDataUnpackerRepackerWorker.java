package ikube.data.wiki;

import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will read the Bzip2 files and repack them into smaller files of one gig each which are easier to read over the network.
 * 
 * @author Michael Couck
 * @since 15.08.2012
 * @version 01.00
 */
public class WikiDataUnpackerRepackerWorker implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerRepackerWorker.class);

	private long reads;
	private long offset;
	private long fileNumber = 1;
	private long size = 1073741824l;
	private CompressorOutputStream compressorOutputStream;

	private File file;

	public WikiDataUnpackerRepackerWorker(final File file) {
		this.file = file;
	}

	@Override
	public void run() {
		InputStream inputStream = null;
		CompressorInputStream compressorInputStream = null;
		try {
			// Check that this file is not already done
			if (new File(getOutputFilePath()).exists()) {
				LOGGER.warn("File already done : " + file);
				return;
			}
			inputStream = new FileInputStream(file);
			compressorInputStream = new BZip2CompressorInputStream(inputStream);
			int read = -1;
			byte[] bytes = new byte[1024 * 1024 * 10];
			while ((read = compressorInputStream.read(bytes)) > -1) {
				write(bytes, read);
			}
		} catch (Exception e) {
			LOGGER.error(null, e);
		} finally {
			FileUtilities.close(compressorOutputStream);
		}
	}

	void write(final byte[] bytes, final int read) {
		reads++;
		if (reads % 10 == 0) {
			LOGGER.info("Reads : " + reads + ", " + offset + ", " + fileNumber + ", " + size);
		}
		try {
			if (offset > size || compressorOutputStream == null) {
				FileUtilities.close(compressorOutputStream);
				File outputFile = getOutputFile();
				OutputStream outputStream = new FileOutputStream(outputFile);
				compressorOutputStream = new CompressorStreamFactory().createCompressorOutputStream("bzip2", outputStream);
				LOGGER.info("New output stream : " + outputFile);
				LOGGER.info("Reads : " + reads + ", " + offset + ", " + fileNumber + ", " + size);
				offset = 0;
				fileNumber++;
			}
			offset += bytes.length;
			compressorOutputStream.write(bytes, 0, read);
		} catch (Exception e) {
			LOGGER.error(null, e);
		}
	}

	private File getOutputFile() {
		return FileUtilities.getFile(getOutputFilePath(), Boolean.FALSE);
	}

	private String getOutputFilePath() {
		StringBuilder bzip2FilePath = new StringBuilder();
		bzip2FilePath.append(file.getAbsolutePath());
		bzip2FilePath.append(".");
		bzip2FilePath.append(fileNumber);
		bzip2FilePath.append(".gig.bz2");
		return bzip2FilePath.toString();
	}

}