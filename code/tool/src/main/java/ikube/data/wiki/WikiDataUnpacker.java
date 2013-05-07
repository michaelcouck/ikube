package ikube.data.wiki;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

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

	private static final String WRITE = "write";
	private static final String UNPACK = "unpack";
	private static final String REPACK = "repack";

	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			printUsage();
		}
		ThreadUtilities.initialize();
		try {
			if (args[0].equals(WRITE)) {
				read7ZandWriteBzip2(args[1]);
			} else if (args[0].equals(UNPACK)) {
				readBz2AndUnpackFiles(args[1]);
			} else if (args[0].equals(REPACK)) {
				readBz2AndWriteBzip2(args[1]);
			} else {
				printUsage();
			}
		} catch (Exception e) {
			printUsage();
			e.printStackTrace();
		}
	}

	private static final void printUsage() {
		System.out.println("Usage   : [" + WRITE + " | " + UNPACK + " | " + REPACK
				+ "] & [directory] & [disk patterns, like 'xfs-' for example]");
		System.out.println("Example to write bz2 files : java -jar ikube.jar write /media/nas/xfs/wiki-history-languages");
		System.out.println("Example to unpack html files from bz2 : java -jar ikube.jar unpack /media/nas/xfs/wiki-history-languages");
		System.out.println("Example to repack bz2 files : java -jar ikube.jar repack /media/nas/xfs/wiki-history-languages");
		System.exit(0);
	}

	/**
	 * This method will read all the Bzip2 files in the specified directory and write them to smaller one gig files that can be read easier
	 * over the network.
	 * 
	 * @param directoryPath the path to the directory where the compressed files are
	 */
	protected static void readBz2AndWriteBzip2(final String directoryPath) {
		File directory = new File(directoryPath);
		List<File> bZip2Files = FileUtilities.findFilesRecursively(directory, new ArrayList<File>(), "bz2");
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (final File bZip2File : bZip2Files) {
			WikiDataUnpackerRepackerWorker wikiDataUnpackerWorker = new WikiDataUnpackerRepackerWorker(bZip2File);
			Future<?> future = ThreadUtilities.submit(null, wikiDataUnpackerWorker);
			futures.add(future);
			if (futures.size() >= 4) {
				ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
				futures.clear();
			}
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

	/**
	 * This method will read the bzip2 files one by one and unpack them onto the external disks.
	 * 
	 * @param directoryPath the path to the files to unpack
	 */
	protected static void readBz2AndUnpackFiles(final String directoryPath) throws Exception {
		File directory = new File(directoryPath);
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < 4; i++) {
			WikiDataUnpackerWorker wikiDataUnpackerWorker = new WikiDataUnpackerWorker(directory);
			Future<?> future = ThreadUtilities.submit(null, wikiDataUnpackerWorker);
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

	/**
	 * This method will read the 7z history of the wiki, unpack the compressed file, break it up into segments of one giga-byte then write
	 * the file to a compressed bzip2 file.
	 * 
	 * @param directoryPath the path to the input files, i.e. the 7z files to convert
	 */
	protected static void read7ZandWriteBzip2(final String directoryPath) throws Exception {
		File directory = new File(directoryPath);
		List<File> files = FileUtilities.findFilesRecursively(directory, new ArrayList<File>(), "7z");
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (final File file : files) {
			WikiDataUnpacker7ZWorker dataUnpacker7ZWorker = new WikiDataUnpacker7ZWorker(file, 1000);
			Future<?> future = ThreadUtilities.submit(null, dataUnpacker7ZWorker);
			futures.add(future);
			if (futures.size() >= 4) {
				ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
				futures.clear();
			}
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}
	
	protected static void read7ZAndUnpackFiles(final String directoryPath, final String... outputDirectories) throws Exception {
		
	}

}