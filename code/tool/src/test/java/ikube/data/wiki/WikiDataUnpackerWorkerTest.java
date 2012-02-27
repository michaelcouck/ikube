package ikube.data.wiki;

import static org.junit.Assert.assertTrue;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WikiDataUnpackerWorkerTest {

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
		FileInputStream fileInputStream = new FileInputStream(WikiDataUnpacker.INPUT_FILE);
		BZip2CompressorInputStream bZip2CompressorInputStream = new BZip2CompressorInputStream(fileInputStream);
		File directory = FileUtilities.getFile(outputDirectoryPath, Boolean.TRUE);
		return new WikiDataUnpackerWorker(directory, bZip2CompressorInputStream, offset, length);
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
