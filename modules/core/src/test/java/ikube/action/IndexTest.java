package ikube.action;

import static org.junit.Assert.assertTrue;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class IndexTest extends BaseActionTest {

	private Index index = new Index();

	@Test
	public void execute() throws Exception {
		long maxAge = indexContext.getMaxAge();

		indexContext.setMaxAge(0);

		boolean done = index.execute(indexContext);
		assertTrue(done);

		indexContext.setMaxAge(maxAge);

		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath());
		FileUtilities.deleteFile(baseIndexDirectory, 1);
	}

}
