package ikube.data.wiki;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
 * This class will unpack the 7z files from Wiki and repack the data into Bzip2 files. Then unpack the Bzip2 files onto the disks.
 * 
 * @author Michael Couck
 * @since at least 14.04.2012
 * @version 01.00
 */
public class WikiDataUnpacker {

	static {
		// Init the logging config
		Logging.configure();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpacker.class);

	public static void main(String[] args) throws Exception {
		// read7ZandWriteBzip2();
		readBz2AndUnpackFiles();
	}

	/**
	 * This method will read the bzip2 files one by one and unpack them onto the external disks.
	 */
	protected static void readBz2AndUnpackFiles() throws Exception {
		// Get the output directories/disks in the media folder
		File[] disks = FileUtilities.findFiles(new File("/media"), new String[] { "disk" });
		// Get the input files that are the Bzip2 files with a big xml in them
		File[] files = FileUtilities.findFiles(new File("/usr/local/wiki/history"), new String[] { "bz2" });
		// Init the executor service with 10 threads so we don't have too many running at the same time
		List<Future<?>> futures = new ArrayList<Future<?>>();
		ExecutorService executorService = Executors.newFixedThreadPool(4);
		int diskIndex = 0;
		for (File file : files) {
			File disk = disks[diskIndex];
			Future<Void> future = executorService.submit(new WikiDataUnpackerWorker(file, disk), null);
			futures.add(future);
			diskIndex = diskIndex >= disks.length ? 0 : ++diskIndex;
			LOGGER.info("Disk : " + disk + ", file : " + file);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

	/**
	 * This method will read the 7z history of the wiki, unpack the compressed file, break it up into segments of one giga-byte then write
	 * the file to a compressed bzip2 file.
	 */
	protected static void read7ZandWriteBzip2() throws Exception {
		SevenZip.initSevenZipFromPlatformJAR();
		// Get the input stream
		final String filePath = "/home/michael/Downloads/enwiki-20100130-pages-meta-history.xml.7z";
		final RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
		final RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFile);
		final ISevenZipInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);
		final ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

		for (ISimpleInArchiveItem simpleInArchiveItem : simpleInArchive.getArchiveItems()) {
			if (!simpleInArchiveItem.isFolder()) {
				ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(new ISequentialOutStream() {

					long reads;
					long offset;
					long file = 1;
					long oneHundredGig = 107374182400l;
					CompressorOutputStream compressorOutputStream;

					@Override
					public int write(byte[] bytes) throws SevenZipException {
						reads++;
						if (reads % 10 == 0) {
							LOGGER.info("Reads : " + reads + ", " + offset + ", " + file + ", " + oneHundredGig);
						}
						try {
							if (offset > oneHundredGig || compressorOutputStream == null) {
								// Get the output stream
								File outputFile = FileUtilities.getFile("/usr/local/wiki/history/enwiki-20100130-pages-meta-history.xml."
										+ file + ".bz2", Boolean.FALSE);
								OutputStream outputStream = new FileOutputStream(outputFile);
								compressorOutputStream = new CompressorStreamFactory().createCompressorOutputStream("bzip2", outputStream);
								LOGGER.info("New output stream : " + outputFile);
								LOGGER.info("Reads : " + reads + ", " + offset + ", " + file + ", " + oneHundredGig);
								offset = 0;
								file += 1;
							}
							offset += bytes.length;
							compressorOutputStream.write(bytes);
						} catch (Exception e) {
							LOGGER.error(null, e);
							throw new SevenZipException(e);
						}
						return bytes.length;
					}
				});
				LOGGER.info("Extract operation : " + extractOperationResult);
			}
		}
	}

}