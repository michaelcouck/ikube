package ikube.data.wiki;

import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Test;

public class WikiDataUnpacker7ZWorkerTest {

	@Test
	public void run() {
		File file = FileUtilities.findFileRecursively(new File("."), "7zip\\.7z");
		WikiDataUnpacker7ZWorker wikiDataUnpacker7ZWorker = new WikiDataUnpacker7ZWorker(file, 0);
		wikiDataUnpacker7ZWorker.run();
		// TODO Verify some unpacked data
	}

}
