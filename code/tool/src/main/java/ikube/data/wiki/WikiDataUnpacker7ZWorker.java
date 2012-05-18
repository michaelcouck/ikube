package ikube.data.wiki;

import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Document this class.
 * 
 * @author Michael Couck
 * @since 09.04.2012
 * @version 01.00
 */
public class WikiDataUnpacker7ZWorker implements Runnable, ISequentialOutStream {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpacker7ZWorker.class);

	private long reads;
	private long offset;
	private long fileNumber = 1;
	private long oneHundredGig = 107374182400l;
	private CompressorOutputStream compressorOutputStream;

	private File file;
	private long throttle;

	public WikiDataUnpacker7ZWorker(final File file, final long throttle) {
		this.file = file;
		this.throttle = throttle;
	}

	@Override
	public void run() {
		try {
			SevenZip.initSevenZipFromPlatformJAR();
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
			RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFile);
			ISevenZipInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);
			ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

			for (ISimpleInArchiveItem simpleInArchiveItem : simpleInArchive.getArchiveItems()) {
				if (!simpleInArchiveItem.isFolder()) {
					ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(this);
					LOGGER.info("Extract operation : " + extractOperationResult);
					FileUtilities.close(compressorOutputStream);
				}
			}

		} catch (Exception e) {
			LOGGER.error(null, e);
		} finally {
			FileUtilities.close(compressorOutputStream);
		}
	}

	@Override
	public int write(byte[] bytes) throws SevenZipException {
		reads++;
		if (reads % 10 == 0) {
			LOGGER.info("Reads : " + reads + ", " + offset + ", " + fileNumber + ", " + oneHundredGig);
		}
		try {
			Thread.sleep(throttle);
			if (offset > oneHundredGig || compressorOutputStream == null) {
				if (compressorOutputStream != null) {
					FileUtilities.close(compressorOutputStream);
				}
				// Get the output stream
				StringBuilder bzip2FilePath = new StringBuilder();
				bzip2FilePath.append(file.getAbsolutePath());
				bzip2FilePath.append(fileNumber);
				bzip2FilePath.append(".bz2");
				File outputFile = FileUtilities.getFile(bzip2FilePath.toString(), Boolean.FALSE);
				OutputStream outputStream = new FileOutputStream(outputFile);
				compressorOutputStream = new CompressorStreamFactory().createCompressorOutputStream("bzip2", outputStream);
				LOGGER.info("New output stream : " + outputFile);
				LOGGER.info("Reads : " + reads + ", " + offset + ", " + fileNumber + ", " + oneHundredGig);
				offset = 0;
				fileNumber += 1;
			}
			offset += bytes.length;
			compressorOutputStream.write(bytes);
		} catch (Exception e) {
			LOGGER.error(null, e);
			throw new SevenZipException(e);
		}
		return bytes.length;
	}

}