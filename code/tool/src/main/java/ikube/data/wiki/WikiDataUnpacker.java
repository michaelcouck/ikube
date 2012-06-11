package ikube.data.wiki;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	private static ExecutorService EXECUTER_SERVICE = Executors.newFixedThreadPool(4);

	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			printUsage();
		}
		try {
			if (args[0].equals(WRITE)) {
				read7ZandWriteBzip2(args[1]);
			} else if (args[0].equals(UNPACK)) {
				readBz2AndUnpackFiles(args[1], args[2]);
			}
		} catch (Exception e) {
			printUsage();
			e.printStackTrace();
		}
	}

	private static final void printUsage() {
		System.out.println("Usage   : [" + WRITE + " | " + UNPACK + "] & [directory] & [disk patterns, like 'xfs-' for example]");
		System.out.println("Example : java -jar ikube.jar write /media/nas/xfs/wiki-history-languages xfs-");
		System.exit(0);
	}

	/**
	 * This method will read the bzip2 files one by one and unpack them onto the external disks.
	 */
	protected static void readBz2AndUnpackFiles(final String directoryPath, final String diskPattern) throws Exception {
		// Get the output directories/disks in the media folder
		File directory = new File(directoryPath);
		List<File> disks = FileUtilities.findFilesRecursively(directory, new ArrayList<File>(), diskPattern);
		// Init the executor service with 10 threads so we don't have too many running at the same time
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (File disk : disks) {
			WikiDataUnpackerWorker wikiDataUnpackerWorker = new WikiDataUnpackerWorker(disk);
			Future<Void> future = EXECUTER_SERVICE.submit(wikiDataUnpackerWorker, null);
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

	/**
	 * This method will read the 7z history of the wiki, unpack the compressed file, break it up into segments of one giga-byte then write
	 * the file to a compressed bzip2 file.
	 * 
	 * @param directoryPath
	 *        the path to the input files, i.e. the 7z files to convert
	 */
	protected static void read7ZandWriteBzip2(final String directoryPath) throws Exception {
		File directory = new File(directoryPath);
		List<File> files = FileUtilities.findFilesRecursively(directory, new ArrayList<File>(), "7z");
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (File file : files) {
			WikiDataUnpacker7ZWorker dataUnpacker7ZWorker = new WikiDataUnpacker7ZWorker(file, 1000);
			Future<Void> future = EXECUTER_SERVICE.submit(dataUnpacker7ZWorker, null);
			futures.add(future);
			Thread.sleep(1000);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

}