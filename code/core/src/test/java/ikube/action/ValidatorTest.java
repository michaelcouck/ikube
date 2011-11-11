package ikube.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.ATest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mockit.Mockit;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
public class ValidatorTest extends ATest {

	// @Mocked(methods = { "sendNotification" })
	private Validator validator;

	public ValidatorTest() {
		super(ValidatorTest.class);
	}

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
		validator = spy(new Validator());
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				logger.info("Message sent : " + invocation);
				return null;
			}
		}).when(validator).sendNotification(anyString(), anyString());
		validator.setClusterManager(clusterManager);
		when(indexContext.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
		FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
	}

	@Test
	public void validate() throws Exception {
		boolean result = validator.execute(indexContext);
		assertFalse("There are no indexes created : ", result);
		// There should be a mail sent because there are no indexes created
		// There should also be a mail sent because the searchable is not opened
		// There should be a mail sent because there is no backup for this index
		int invocations = 3;
		verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());

		File latestIndexDirectory = createIndex(indexContext, "a little sentence");
		File serverIndexDirectory = new File(latestIndexDirectory, ip);
		result = validator.execute(indexContext);
		assertFalse("There is an index created but no backup : ", result);
		// There should be no mail sent because there is an index generated
		// There should be a mail sent because there is no backup for this index
		invocations++;
		verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());

		Backup backup = new Backup();
		backup.setClusterManager(clusterManager);
		backup.execute(indexContext);
		result = validator.execute(indexContext);
		assertTrue("There is an index created : ", result);

		Directory directory = FSDirectory.open(serverIndexDirectory);
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		result = validator.execute(indexContext);
		assertFalse("The index is locked : ", result);
		// There should be a message sent to notify that there is an index being
		// generated and that the current index is not current
		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);
		invocations++;
		verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());

		// Delete one file in the index and there should be an exception
		List<File> files = FileUtilities.findFilesRecursively(latestIndexDirectory, new ArrayList<File>(), "segments");
		for (File file : files) {
			FileUtilities.deleteFile(file, 1);
		}
		result = validator.execute(indexContext);
		assertFalse("The index is corrupt : ", result);
		// There should be a mail sent because the index is corrupt
		invocations++;
		invocations++;
		verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());

		result = validator.execute(indexContext);
		// There should be another mail sent
		invocations++;
		invocations++;
		verify(validator, Mockito.times(invocations)).sendNotification(anyString(), anyString());
	}

}