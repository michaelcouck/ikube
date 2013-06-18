package ikube.comp;

import ikube.Constants;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.document.CompressionTools;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressionToolsTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompressionToolsTest.class);

	@Test
	public void compress() {
		File file = FileUtilities.findFileRecursively(new File("."), "txt.txt");
		String content = FileUtilities.getContents(file, Constants.ENCODING);
		LOGGER.info("Before : " + content.getBytes().length);
		byte[] compressed = CompressionTools.compress(content.getBytes());
		LOGGER.info("After : " + compressed.length);

		content = "hello world";
		LOGGER.info("Before : " + content.getBytes().length);
		compressed = CompressionTools.compress(content.getBytes());
		LOGGER.info("After : " + compressed.length);
	}

}