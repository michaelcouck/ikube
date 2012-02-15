package ikube.data.wiki;

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

public class WikiDataUnpacker {

	public static void main(String[] args) throws Exception {
		readBz2Tar();
	}

	protected static void readBz2Tar() throws Exception {
		Collection<Thread> threads = new ArrayList<Thread>();
		int length = 100000;
		int offset = 0;
		for (int i = 0; i < 3; i++) {
			FileInputStream in = new FileInputStream("C:/Tmp/sumo.4000.2012.02.07.15.20.48.877.21763386990804.csv.bz2");
			BZip2CompressorInputStream inputStream = new BZip2CompressorInputStream(in);
			System.out.println("Input stream : " + inputStream.getClass());
			Thread thread = new Thread(new WikiDataUnpackerWorker(inputStream, offset, length));
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
				System.out.println("Input stream : " + inputStream.getClass());
				Thread thread = new Thread(new WikiDataUnpackerWorker(inputStream, offset, length));
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
