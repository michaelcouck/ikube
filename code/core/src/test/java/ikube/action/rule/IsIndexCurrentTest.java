package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.mock.IndexManagerMock;

import java.io.File;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO Implement me!
 * 
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class IsIndexCurrentTest extends ATest {

	/** Class under test. */
	private IsIndexCurrent isIndexCurrentRule = new IsIndexCurrent();

	public IsIndexCurrentTest() {
		super(IsIndexCurrentTest.class);
	}

	@Before
	public void beforeClass() {
		Mockit.setUpMock(IndexManagerMock.class);
	}

	@After
	public void afterClass() {
		Mockit.tearDownMocks();
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