package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.mock.FSDirectoryMock;
import ikube.mock.IndexReaderMock;
import ikube.mock.IndexWriterMock;

import java.io.IOException;

import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 29.03.2011
 * @version 01.00
 */
public class DirectoryExistsAndNotLockedTest extends AbstractTest {

	private DirectoryExistsAndNotLocked existsAndNotLocked;

	@Before
	public void before() {
		Mockit.setUpMocks(IndexWriterMock.class, IndexReaderMock.class, FSDirectoryMock.class);
		existsAndNotLocked = new DirectoryExistsAndNotLocked();
	}

	@After
	public void afterClass() {
		Mockit.tearDownMocks();
	}

	@Test
	public void evaluate() throws IOException {
		IndexWriterMock.setIsLocked(Boolean.FALSE);
		IndexReaderMock.setIndexExists(Boolean.TRUE);
		boolean existsAndIsNotLocked = existsAndNotLocked.evaluate(null);
		assertTrue(existsAndIsNotLocked);

		IndexWriterMock.setIsLocked(Boolean.TRUE);
		IndexReaderMock.setIndexExists(Boolean.FALSE);
		existsAndIsNotLocked = existsAndNotLocked.evaluate(null);
		assertFalse(existsAndIsNotLocked);
	}

}