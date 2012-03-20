package ikube.data.wiki;

import ikube.toolkit.FileUtilities;

import java.io.File;
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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class WikiDataUnpackerWorkerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerWorkerTest.class);

	private String inputFile = "/home/michael/Downloads/enwiki-20100130-pages-meta-history.xml.7z";

	private String outputDirectoryPath = "/tmp/wiki/test";
	private File outputDirectory;
	/** Class under test. */
	private WikiDataUnpackerWorker wikiDataUnpackerWorker;

	@Before
	public void before() {
		outputDirectory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
		FileUtilities.deleteFile(outputDirectory, 1);
		outputDirectory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
	}

	@After
	public void after() {
		// FileUtilities.deleteFile(outputDirectory, 1);
	}

	@Test
	public void run() throws Exception {
		wikiDataUnpackerWorker = new WikiDataUnpackerWorker();
		wikiDataUnpackerWorker.setDirectory(outputDirectory);

		SevenZip.initSevenZipFromPlatformJAR();
		RandomAccessFile randomAccessFile = new RandomAccessFile(inputFile, "r");
		RandomAccessFileInStream randomAccessFileInStream = new RandomAccessFileInStream(randomAccessFile);
		// randomAccessFileInStream.seek(1000000, 1);
		ISevenZipInArchive inArchive = SevenZip.openInArchive(null, randomAccessFileInStream);
		ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
		for (ISimpleInArchiveItem simpleInArchiveItem : simpleInArchive.getArchiveItems()) {
			if (!simpleInArchiveItem.isFolder()) {
				ExtractOperationResult extractOperationResult = simpleInArchiveItem.extractSlow(new ISequentialOutStream() {
					@Override
					public int write(byte[] bytes) throws SevenZipException {
						try {
							wikiDataUnpackerWorker.unpack(bytes, 0, bytes.length);
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

}