package ikube.data.wiki;

import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Random;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will unpack a 7Zip file and write the contents one file at a time to the output directory. The output directories are spread over the disks that
 * are accepted in the constructor, and the distribution over the disks is random. Folders are created using the system time, and new output folders are created
 * when the current directory has a few thousand files in it.
 * 
 * @author Michael Couck
 * @since 07.05.2013
 * @version 01.00
 */
public class WikiDataUnpacker7ZFileWorker implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpacker7ZFileWorker.class);

	public static final String PAGE_START = "<revision>";
	public static final String PAGE_FINISH = "</revision>";

	/** The maximum number of files to write to a directory. */
	private int threshold = 10000;
	/** The holder of the number files in a directory, could be inherited. */
	private int numberOfFilesInFolder = 0;

	/** The input SevenZip file. */
	private File inputFile;
	/** The disks that the data will be written to. */
	private File[] outputDisks;

	/**
	 * Constructor takes the input file, compressed and the disks that will be written to.
	 * 
	 * @param inputFile the input compressed file
	 * @param outputDisks the output disks
	 */
	public WikiDataUnpacker7ZFileWorker(final File inputFile, final File... outputDisks) {
		this.inputFile = inputFile;
		this.outputDisks = outputDisks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		final StringBuilder stringBuilder = new StringBuilder();
		// This is the implementation of the output stream of data from de-compressing
		// the data using LZMA, via the seven Zip implementation
		class SequentialOutStream implements ISequentialOutStream {

			File currentDirectory;

			/**
			 * {@inheritDoc}
			 */
			@Override
			public int write(final byte[] bytes) throws SevenZipException {
				// Split the bytes up into segments to write to a file
				stringBuilder.append(new String(bytes));
				int start = stringBuilder.indexOf(PAGE_START);
				int end = stringBuilder.indexOf(PAGE_FINISH);
				if (start > 0 && end > 0) {
					String data = stringBuilder.substring(start, end);
					// Get a new file to write to
					File outputFile = getNextFile(currentDirectory);
					currentDirectory = outputFile.getParentFile();
					FileUtilities.setContents(outputFile, data.getBytes());
				}
				return bytes.length;
			}
		}

		try {
			// Choose the platform for the native libraries
			SevenZip.initSevenZipFromPlatformJAR();
			RandomAccessFile randomAccessFile = new RandomAccessFile(inputFile, "r");
			RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFile);
			ISevenZipInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);
			ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
			for (ISimpleInArchiveItem simpleInArchiveItem : simpleInArchive.getArchiveItems()) {
				if (!simpleInArchiveItem.isFolder()) {
					ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(new SequentialOutStream());
					LOGGER.info("Extract operation : " + extractOperationResult);
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method will get the next file to write to. If there are more than a few thousand files in the directory then a new one is created. The choice of
	 * output disks is purely random, with the expectation that the directories/files will eventually be evenly distributed ove the disks.
	 * 
	 * @param currentDirectory the current output directory, could be null
	 * @return the output file, on one of the output disks, and in a folder possibly freshly created
	 */
	private File getNextFile(final File currentDirectory) {
		File outputDirectory = currentDirectory;
		if (outputDirectory == null || numberOfFilesInFolder > threshold) {
			// Create a new directory on a random disk
			int outputDiskIndex = new Random().nextInt(outputDisks.length);
			File outputDisk = outputDisks[outputDiskIndex];
			String outputDirectoryPath = outputDisk.getAbsolutePath() + File.separator + System.currentTimeMillis();
			outputDirectory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
			numberOfFilesInFolder = 0;
			LOGGER.info("New directory : " + outputDirectory);
		}
		String outputDirectoryPath = outputDirectory.getAbsolutePath();
		String filePath = new StringBuilder(outputDirectoryPath).append(File.separator).append(System.currentTimeMillis()).append(".html").toString();
		return FileUtilities.getFile(filePath, Boolean.FALSE);
	}

}
