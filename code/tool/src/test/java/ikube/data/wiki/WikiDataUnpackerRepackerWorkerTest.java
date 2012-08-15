package ikube.data.wiki;

import static org.junit.Assert.assertNotNull;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WikiDataUnpackerRepackerWorkerTest {

	int throttle = 0;
	private File file;
	private String baseDirectory = ".";
	private WikiDataUnpackerRepackerWorker wikiDataUnpackerRepackerWorker;

	@Before
	public void before() {
		Logging.configure();
		file = FileUtilities.findFileRecursively(new File(baseDirectory), "bz2");
		wikiDataUnpackerRepackerWorker = new WikiDataUnpackerRepackerWorker(file, throttle);
	}

	@After
	public void after() {
		File outputFile = FileUtilities.findFileRecursively(new File(baseDirectory), Boolean.FALSE, "gig.bz2");
		if (outputFile != null) {
			FileUtilities.deleteFile(outputFile, 1);
		}
	}

	@Test
	public void run() {
		wikiDataUnpackerRepackerWorker.run();
		// Verify that there is another file with the same name
		File outputFile = FileUtilities.findFileRecursively(new File(baseDirectory), Boolean.FALSE, "gig.bz2");
		assertNotNull("There should be an output file at least : ", outputFile);
	}

}
