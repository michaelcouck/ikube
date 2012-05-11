package ikube.data.wiki;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileFilter;
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

	private static ExecutorService EXECUTER_SERVICE = Executors.newFixedThreadPool(4);

	public static void main(String[] args) throws Exception {
		// read7ZandWriteBzip2();
		readBz2AndUnpackFiles();
	}

	/**
	 * This method will read the bzip2 files one by one and unpack them onto the external disks.
	 */
	protected static void readBz2AndUnpackFiles() throws Exception {
		// Get the output directories/disks in the media folder
		File[] disks = new File("/media").listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
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
	 */
	protected static void read7ZandWriteBzip2() throws Exception {
		List<File> files = FileUtilities.findFilesRecursively(new File("/media/disk-with-compressed-wiki-language-files"),
				new ArrayList<File>(), "7z");
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (File file : files) {
			WikiDataUnpacker7ZWorker dataUnpacker7ZWorker = new WikiDataUnpacker7ZWorker(file);
			Future<Void> future = EXECUTER_SERVICE.submit(dataUnpacker7ZWorker, null);
			futures.add(future);
		}
		ThreadUtilities.waitForFutures(futures, Long.MAX_VALUE);
	}

}