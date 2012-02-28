package ikube.data.wiki;

import static org.junit.Assert.assertTrue;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpackerWorkerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerWorkerTest.class);
	
	private String outputDirectoryPath = "/tmp/wiki/test";
	private WikiDataUnpackerWorker wikiDataUnpackerWorker;

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(outputDirectoryPath), 1);
	}

	@After
	public void after() {
		// FileUtilities.deleteFile(new File(outputDirectoryPath), 1);
	}

	private WikiDataUnpackerWorker getWikiDataUnpackerWorker(final int offset, final int length) throws Exception {
		SevenZip.initSevenZipFromPlatformJAR();
		RandomAccessFile randomAccessFile = new RandomAccessFile(WikiDataUnpacker.INPUT_FILE, "r");
		RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFile);
		ISevenZipInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);

		InputStream inputStream = new ByteArrayInputStream(new byte[0]);
		ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
		for (ISimpleInArchiveItem simpleInArchiveItem : simpleInArchive.getArchiveItems()) {
			if (!simpleInArchiveItem.isFolder()) {
				ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(new ISequentialOutStream() {
					@Override
					public int write(byte[] bytes) throws SevenZipException {
						LOGGER.info("Output : " + new String(bytes));
						return bytes.length;
					}
				});
				LOGGER.info("Extract operation : " + extractOperationResult);
			}
		}
		File directory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
		return new WikiDataUnpackerWorker(directory, inputStream, offset, length);
	}

	@Test
	public void run() throws Exception {
		wikiDataUnpackerWorker = getWikiDataUnpackerWorker(0, 1000000);
		wikiDataUnpackerWorker.run();
		// Verify that there are files in the output directory
		File outputDirectory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
		File[] outputFiles = outputDirectory.listFiles();
		int outputFilesLength = outputFiles.length;
		assertTrue("There must be some files in the output directory : ", outputFilesLength > 0);

		wikiDataUnpackerWorker = getWikiDataUnpackerWorker(10000000, 1000000);
		wikiDataUnpackerWorker.run();

		outputDirectory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
		outputFiles = outputDirectory.listFiles();
		int newOutputFilesLength = outputFiles.length;
		assertTrue("There must be more files in the output directory : ", newOutputFilesLength > outputFilesLength);
	}

}
