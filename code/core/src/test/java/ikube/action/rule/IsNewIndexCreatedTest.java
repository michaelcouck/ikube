package ikube.action.rule;

import ikube.AbstractTest;
import ikube.action.Open;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import org.apache.lucene.index.IndexReaderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * This test is for the {@link IsNewIndexCreated} class that will check if there is a newer index than the one that is
 * opened in the index searcher.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11.06.11
 */
public class IsNewIndexCreatedTest extends AbstractTest {

	@After
	public void after() {
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void evaluate() throws Exception {
		IndexReaderContext indexReaderContext = Mockito.mock(IndexReaderContext.class);
		Mockito.when(indexReaderContext.children()).thenReturn(Arrays.asList(indexReaderContext));
		Mockito.when(indexSearcher.getTopReaderContext()).thenReturn(indexReaderContext);

		IsNewIndexCreated isNewIndexCreated = new IsNewIndexCreated();
		boolean result = isNewIndexCreated.evaluate(indexContext);
		assertFalse("There is no index created : ", result);

		createIndexFileSystem(indexContext, "Some data");

		result = isNewIndexCreated.evaluate(indexContext);
		assertTrue("The index is created : ", result);

		result = isNewIndexCreated.evaluate(indexContext);
		assertTrue("The latest index is already opened : ", result);

		createIndexFileSystem(indexContext, "Some more data");
		result = isNewIndexCreated.evaluate(indexContext);
		assertTrue("There is a new index, and the searcher is open on the old one : ", result);
	}

}