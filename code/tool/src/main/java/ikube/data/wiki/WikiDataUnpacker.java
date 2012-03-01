package ikube.data.wiki;

import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileFilter;
import java.io.RandomAccessFile;

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

public class WikiDataUnpacker {

	public static final String[] OUTPUT_DIRECTORIES = { "/usr/local/wiki/history/one", "/usr/local/wiki/history/two",
			"/usr/local/wiki/history/three", "/usr/local/wiki/history/four", "/usr/local/wiki/history/five", "/usr/local/wiki/history/six",
			"/usr/local/wiki/history/seven", "/usr/local/wiki/history/eight", "/usr/local/wiki/history/nine", "/usr/local/wiki/history/ten" };
	public static final String INPUT_FILE = "/home/michael/Downloads/enwiki-20100130-pages-meta-history.xml.7z";

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpacker.class);

	public static void main(String[] args) throws Exception {
		readBz2Tar();
	}

	protected static void readBz2Tar() throws Exception {
		SevenZip.initSevenZipFromPlatformJAR();
		RandomAccessFile randomAccessFile = new RandomAccessFile(INPUT_FILE, "r");
		RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFile);
		ISevenZipInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);
		ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
		for (ISimpleInArchiveItem simpleInArchiveItem : simpleInArchive.getArchiveItems()) {
			if (!simpleInArchiveItem.isFolder()) {
				ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(new ISequentialOutStream() {
					private int documents;
					private int directories;
					private int outputDirectoryIndex = 0;
					private WikiDataUnpackerWorker wikiDataUnpackerWorker;

					@Override
					public int write(byte[] bytes) throws SevenZipException {
						if (wikiDataUnpackerWorker == null || documents >= 10000) {
							if (directories >= 10000) {
								directories = 0;
								outputDirectoryIndex++;
							}
							File outputDirectory = FileUtilities.getFile(OUTPUT_DIRECTORIES[outputDirectoryIndex], Boolean.TRUE);
							wikiDataUnpackerWorker = new WikiDataUnpackerWorker(outputDirectory);
							documents = 0;
							directories++;
							LOGGER.info("Output directory : " + outputDirectory);
						}
						try {
							documents += wikiDataUnpackerWorker.unpack(bytes);
						} catch (Exception e) {
							LOGGER.error(null, e);
						}
						return bytes.length;
					}
				});
				LOGGER.info("Extract operation : " + extractOperationResult);
			}
		}
	}

	private long getDirectoryOffset(final File directory) {
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

}