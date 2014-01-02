package ikube.action.rule;

import ikube.AbstractTest;
import ikube.mock.IndexManagerMock;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29.03.2011
 */
public class IsIndexCurrentTest extends AbstractTest {

	/**
	 * Class under test.
	 */
	private IsIndexCurrent isIndexCurrentRule = new IsIndexCurrent();

	@Before
	public void before() {
		Mockit.setUpMock(IndexManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(IndexManagerMock.class);
	}

	@Test
	public void evaluate() {
		String indexDirectory = "./indexes/index/";
		File latestIndexDirectory = new File(indexDirectory + (System.currentTimeMillis() - (1000 * 60 * 60 * 10)));
		IndexManagerMock.setLatestIndexDirectory(latestIndexDirectory);

		boolean isIndexCurrent = isIndexCurrentRule.evaluate(indexContext);
		assertFalse(isIndexCurrent);

		latestIndexDirectory = new File(indexDirectory + System.currentTimeMillis());
		IndexManagerMock.setLatestIndexDirectory(latestIndexDirectory);
		isIndexCurrent = isIndexCurrentRule.evaluate(indexContext);
		assertTrue(isIndexCurrent);
	}

}