package ikube.data.wiki;

import ikube.toolkit.FileUtilities;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;

public class WikiDataUnpacker7ZFileWorkerTest {

	static final Logger LOGGER = Logger.getLogger(WikiDataUnpacker7ZFileWorkerTest.class);

	@Test
	public void run() throws Exception {
		File file = FileUtilities.findFileRecursively(new File("."), "7z");
		File[] outputDisks = File.listRoots();
		WikiDataUnpacker7ZFileWorker wikiDataUnpacker7ZWorker = new WikiDataUnpacker7ZFileWorker(file, outputDisks);
		wikiDataUnpacker7ZWorker.run();
		// TODO Check that some files have been written to a folder
	}

}
