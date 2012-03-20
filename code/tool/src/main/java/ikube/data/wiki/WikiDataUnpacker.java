package ikube.data.wiki;

import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;

import java.io.File;
import java.io.FileInputStream;
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
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpacker {

	static {
		Logging.configure();
	}

	public static final String[] OUTPUT_DIRECTORIES = { "/mnt/disk-one/" };
	public static final String[] INPUT_FILES = { "/usr/local/wiki/enwiki-20100130-pages-meta-history.xml.7z.bz2.001" };

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpacker.class);

	public static void main(String[] args) throws Exception {
		read7ZandWriteBzip2();
	}

	protected static void readBz2() throws Exception {
		for (int i = 0; i < OUTPUT_DIRECTORIES.length; i++) {
			int count = 0;
			int directoryNumber = 1;
			FileInputStream in = new FileInputStream(INPUT_FILES[i]);
			BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
			File baseDirectory = FileUtilities.getFile(OUTPUT_DIRECTORIES[i], Boolean.TRUE);
			File outputDirectory = FileUtilities.getFile(baseDirectory.getAbsolutePath() + File.separator + directoryNumber, Boolean.TRUE);
			WikiDataUnpackerWorker wikiDataUnpackerWorker = new WikiDataUnpackerWorker();
			wikiDataUnpackerWorker.setDirectory(outputDirectory);
			int read = -1;
			byte[] bytes = new byte[1024 * 1024];
			while ((read = bzIn.read(bytes)) > -1) {
				count += wikiDataUnpackerWorker.unpack(bytes, 0, read);
				if (count >= 10000) {
					count = 0;
					directoryNumber++;
					outputDirectory = FileUtilities.getFile(baseDirectory.getAbsolutePath() + File.separator + directoryNumber,
							Boolean.TRUE);
					wikiDataUnpackerWorker.setDirectory(outputDirectory);
				}
			}
		}
	}

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