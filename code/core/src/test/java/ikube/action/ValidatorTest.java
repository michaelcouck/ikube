package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mockit.Mocked;
import mockit.Mockit;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
public class ValidatorTest extends ATest {

	@Mocked(methods = { "sendNotification" })
	private Validator validator;

	public ValidatorTest() {
		super(ValidatorTest.class);
	}

	@Before
	public void before() {
		// validator = spy(new Validator());
		// doAnswer(new Answer<Object>() {
		// @Override
		// public Object answer(InvocationOnMock invocation) throws Throwable {
		// logger.info("Invocation : " + invocation);
		// return null;
		// }
		// }).when(validator).sendNotification(any(IndexContext.class), anyString(), anyString());
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void validate() throws Exception {
		boolean result = validator.execute(INDEX_CONTEXT);
		assertFalse("There are no indexes created : ", result);
		// There should be one mail sent because there are no indexes created

		File latestIndexDirectory = createIndex(INDEX_CONTEXT, "a little sentence");
		File serverIndexDirectory = new File(latestIndexDirectory, IP);
		result = validator.execute(INDEX_CONTEXT);
		assertTrue("There is an index created : ", result);
		// There should be no mail sent because there is an index generated

		Directory directory = FSDirectory.open(serverIndexDirectory);
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		result = validator.execute(INDEX_CONTEXT);
		assertFalse("The index is locked : ", result);
		// There should be no mail sent because the index is locked, i.e. being generated
		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		// Delete one file in the index and there should be an exception
		List<File> files = FileUtilities.findFilesRecursively(latestIndexDirectory, new ArrayList<File>(), "segments");
		for (File file : files) {
			FileUtilities.deleteFile(file, 1);
		}
		result = validator.execute(INDEX_CONTEXT);
		assertFalse("The index is corrupt : ", result);
		// There should be a mail sent because the index is corrupt

		FileUtilities.deleteFile(latestIndexDirectory, 1);
	}
}