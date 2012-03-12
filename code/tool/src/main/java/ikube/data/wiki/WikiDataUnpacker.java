package ikube.data.wiki;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.Logging;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpacker {

	static {
		Logging.configure();
	}

	public static final String[] OUTPUT_DIRECTORIES = {
	// "/dev/sdb1"
	"/mnt/disk-one/"
	// , "/usr/local/wiki/history/two",
	// "/usr/local/wiki/history/three", "/usr/local/wiki/history/four", "/usr/local/wiki/history/five", "/usr/local/wiki/history/six",
	// "/usr/local/wiki/history/seven", "/usr/local/wiki/history/eight", "/usr/local/wiki/history/nine", "/usr/local/wiki/history/ten"
	};
	public static final String INPUT_FILE = "/home/michael/Downloads/enwiki-20100130-pages-meta-history.xml.7z";

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpacker.class);

	public static void main(String[] args) throws Exception {
		readBz2Tar();
	}

	protected static void readBz2Tar() throws Exception {
		SevenZip.initSevenZipFromPlatformJAR();
		final long offset = 0;
		final RandomAccessFile randomAccessFile = new RandomAccessFile(INPUT_FILE, "r");
		final RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFile);
		final ISevenZipInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);
		final ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
		randomAccessFileInStream.seek(offset, RandomAccessFileInStream.SEEK_SET);
		for (ISimpleInArchiveItem simpleInArchiveItem : simpleInArchive.getArchiveItems()) {
			if (!simpleInArchiveItem.isFolder()) {
				final StringBuilder stringBuilder = new StringBuilder();
				ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(new ISequentialOutStream() {
					int count = 1;
					String PAGE_START = "<revision>";
					String PAGE_FINISH = "</revision>";
					File baseDirectory = FileUtilities.getFile(OUTPUT_DIRECTORIES[0], Boolean.TRUE);
					File outputDirectory = FileUtilities.getFile(baseDirectory.getAbsolutePath() + IConstants.SEP + "0", Boolean.TRUE);
					long directoryOffset;
					{
						directoryOffset = getDirectoryOffset(baseDirectory);
						LOGGER.info("Starting at directory offset : " + directoryOffset);
					}
					@Override
					public int write(byte[] bytes) throws SevenZipException {
						String string = new String(bytes, 0, bytes.length, Charset.forName(IConstants.ENCODING));
						stringBuilder.append(string);
						while (true) {
							int startOffset = stringBuilder.indexOf(PAGE_START);
							int endOffset = stringBuilder.indexOf(PAGE_FINISH);
							if (startOffset == -1 || endOffset == -1) {
								break;
							}
							if (endOffset <= startOffset) {
								startOffset = endOffset;
							}
							endOffset += PAGE_FINISH.length();
							stringBuilder.delete(startOffset, endOffset);
							if (count % 10000 == 0) {
								String outputDirectoryPath = baseDirectory.getAbsolutePath() + IConstants.SEP + count;
								outputDirectory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
								try {
									LOGGER.info("Count : " + count + ", " + outputDirectory + ", " + randomAccessFile.getFilePointer());
								} catch (IOException e) {
									LOGGER.error("IOException getting the offset in the files : ", e);
								}
							}
							if (count >= directoryOffset) {
								String segment = stringBuilder.substring(startOffset, endOffset);
								String hash = Long.toString(HashUtilities.hash(segment));
								String filePath = outputDirectory.getAbsolutePath() + File.separator + hash + ".html";
								FileUtilities.setContents(filePath, segment.getBytes(Charset.forName(IConstants.ENCODING)));
							}
							count++;
						}
						return bytes.length;
					}
				});
				LOGGER.info("Extract operation : " + extractOperationResult);
			}
		}
	}

	protected static long getDirectoryOffset(final File directory) {
		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		long directoryOffset = 0;
		for (File file : files) {
			if (!StringUtils.isNumeric(file.getName())) {
				continue;
			}
			if (Long.parseLong(file.getName()) > directoryOffset) {
				directoryOffset = Long.parseLong(file.getName());
			}
		}
		return directoryOffset;
	}

}