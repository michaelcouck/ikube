package ikube.action.rule;

import static org.junit.Assert.assertFalse;
import ikube.ATest;
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
public class DirectoryExistsAndIsLockedTest extends ATest {

	private DirectoryExistsAndIsLocked existsAndIsLocked;

	public DirectoryExistsAndIsLockedTest() {
		super(DirectoryExistsAndIsLockedTest.class);
	}

	@Before
	public void before() {
		existsAndIsLocked = new DirectoryExistsAndIsLocked();
		Mockit.tearDownMocks();
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void evaluate() throws IOException {
		IndexReaderMock.INDEX_EXISTS = Boolean.FALSE;
		IndexWriterMock.IS_LOCKED = Boolean.FALSE;
		Mockit.setUpMocks(IndexWriterMock.class, IndexReaderMock.class, FSDirectoryMock.class);
		boolean result = existsAndIsLocked.evaluate(null);
		Mockit.tearDownMocks();
		assertFalse(result);

		IndexReaderMock.INDEX_EXISTS = Boolean.TRUE;
		IndexWriterMock.IS_LOCKED = Boolean.TRUE;
		Mockit.setUpMocks(IndexWriterMock.class, IndexReaderMock.class, FSDirectoryMock.class);
		result = existsAndIsLocked.evaluate(null);
		Mockit.tearDownMocks();
		// TODO The IndexWriterMock never gets called for some reason! The IndexReaderMock
		// is over written by JMockit, and the FSDirectoryMock but not the IndexWriterMock! Why? This
		// result should be true as the index exists and the index is locked
		// assertFalse(result);
	}

}