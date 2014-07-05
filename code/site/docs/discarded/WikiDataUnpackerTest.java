package ikube.data.wiki;

import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Test;

public class WikiDataUnpackerTest {

	@Test
	public void main() throws Exception {
		String[] args = {};
		WikiDataUnpacker.main(args);
		File inputFile = FileUtilities.findFileRecursively(new File("."), "7zip.7z");
		String inputFilePath = FileUtilities.cleanFilePath(inputFile.getAbsolutePath());
		String outputDirectoryPath = FileUtilities.cleanFilePath(inputFile.getParentFile().getAbsolutePath());
		args = new String[] { WikiDataUnpacker.UNPACK_SINGLES, inputFilePath, outputDirectoryPath };
		WikiDataUnpacker.main(args);
		// Nothing to do here, just no exception expected
	}

}
