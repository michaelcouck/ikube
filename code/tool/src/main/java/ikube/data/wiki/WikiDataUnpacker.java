package ikube.data.wiki;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpacker {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpacker.class);
	private static final String[] OUTPUT_DIRECTORIES = { "/usr/local/wiki/history/one", "/usr/local/wiki/history/two" };
	private static final String INPUT_FILE = "/home/michael/Downloads/enwiki-20100130-pages-meta-history.xml.7z";

	public static void main(String[] args) throws Exception {
		readBz2Tar();
	}

	protected static void readBz2Tar() throws Exception {
		int offset = 0;
		int length = 100000;
		Collection<Thread> threads = new ArrayList<Thread>();
		for (String outputDirectory : OUTPUT_DIRECTORIES) {
			FileInputStream in = new FileInputStream(INPUT_FILE);
			BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(in);
			LOGGER.info("Input stream : " + inputStream.getClass());
			File outputDirectoryFile = FileUtilities.getFile(outputDirectory, Boolean.TRUE);
			WikiDataUnpackerWorker wikiDataUnpackerWorker = new WikiDataUnpackerWorker(inputStream, offset, length, outputDirectoryFile);
			Thread thread = new Thread(wikiDataUnpackerWorker);
			threads.add(thread);
			thread.start();
			offset += length;
		}
		ThreadUtilities.waitForThreads(threads);
	}

	protected static void readZip() throws Exception {
		// Method 2: Works on zip files.
		// 1) Open the zip file
		// 2) Open several readers on the input stream
		// 3) Process the stream in the different threads
		ZipFile zipFile = new ZipFile(new File("C:/Tmp/zip.zip"));
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			Collection<Thread> threads = new ArrayList<Thread>();
			int length = 100000;
			int offset = 0;
			for (int i = 0; i < 3; i++) {
				final InputStream inputStream = zipFile.getInputStream(zipEntry);
				LOGGER.info("Input stream : " + inputStream.getClass());
				Thread thread = new Thread(new WikiDataUnpackerWorker(inputStream, offset, length, FileUtilities.getFile("/tmp",
						Boolean.TRUE)));
				thread.start();
				threads.add(thread);
				offset += length;
			}
			ThreadUtilities.waitForThreads(threads);
		}
	}

	protected static void unpackData() {
		// Method 1: Will take too long.
		// 1) Open the zip file
		// 2) Read n bytes from the input stream
		// 3) Write the bytes to a file on a disk
		// 4) Start a thread to parse the xml on that file
		// 5) Goto 2

		// Notes:
		// When the thread is finished it must delete the file
	}

}