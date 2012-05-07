package ikube.data.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiDataUnpackerWorkerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiDataUnpackerWorkerTest.class);

	private File disk;
	private File bZip2File;
	private WikiDataUnpackerWorker wikiDataUnpackerWorker;

	@Before
	public void before() {
		bZip2File = FileUtilities.findFileRecursively(new File("."), Boolean.FALSE, "bzip2");
		disk = bZip2File.getParentFile();
		wikiDataUnpackerWorker = new WikiDataUnpackerWorker(disk);
	}

	@Test
	public void getFileHashes() {
		Set<String> fileHashes = wikiDataUnpackerWorker.getFileHashes(bZip2File);
		LOGGER.info("File hashes : " + fileHashes);
		assertTrue(fileHashes.contains("1234567890"));
		assertTrue(fileHashes.contains("1234567891"));
		assertTrue(fileHashes.contains("1234567893"));
	}

	@Test
	public void unpack() throws Exception {
		String content = "The data for the revision.";
		String outputDirectoryPath = "./unpacked";
		Set<String> fileHashes = wikiDataUnpackerWorker.getFileHashes(bZip2File);
		File outputDirectory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);

		StringBuilder stringBuilder = new StringBuilder(WikiDataUnpackerWorker.PAGE_START);
		stringBuilder.append(content);
		stringBuilder.append(WikiDataUnpackerWorker.PAGE_FINISH);

		try {
			int count = wikiDataUnpackerWorker.unpack(outputDirectory, stringBuilder, fileHashes);
			assertEquals(1, count);
			File unpackedFile = FileUtilities.findFileRecursively(outputDirectory, "html");
			String unpackedFileContents = FileUtilities.getContents(unpackedFile, Integer.MAX_VALUE).toString();
			assertTrue(unpackedFileContents.contains(content));
		} finally {
			FileUtilities.deleteFile(outputDirectory, 1);
		}

		fileHashes.add(Long.toString(HashUtilities.hash(stringBuilder.toString())));
		outputDirectory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
		try {
			int count = wikiDataUnpackerWorker.unpack(outputDirectory, stringBuilder, fileHashes);
			assertEquals(0, count);
			File unpackedFile = FileUtilities.findFileRecursively(outputDirectory, "html");
			assertNull(unpackedFile);
		} finally {
			FileUtilities.deleteFile(outputDirectory, 1);
		}
	}
}