package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.toolkit.FileUtilities;

import java.io.File;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 14.07.11
 * @version 01.00
 */
public class IsThisIndexCreatedTest extends ATest {

	private IsThisIndexCreated isThisIndexCreated;

	public IsThisIndexCreatedTest() {
		super(IsThisIndexCreatedTest.class);
	}

	@Before
	public void before() {
		isThisIndexCreated = new IsThisIndexCreated();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void execute() {
		boolean isIndexCreated = isThisIndexCreated.evaluate(INDEX_CONTEXT);
		assertFalse("This index is not created yet : ", isIndexCreated);

		createIndex(INDEX_CONTEXT, "Some data : ");

		isIndexCreated = isThisIndexCreated.evaluate(INDEX_CONTEXT);
		assertTrue("This index is not created yet : ", isIndexCreated);
	}

}
