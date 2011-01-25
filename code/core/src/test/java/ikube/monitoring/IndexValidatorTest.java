package ikube.monitoring;

import ikube.BaseTest;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test has to be verified visually using a mail client to the account. It is possible to write a mail client to check that the mails
 * are sent but this is a lot of work for very little return. Or try to mock the mail client somehow, replace it in the Spring context.
 * 
 * @author Michael Couck
 * @since 15.01.11
 * @version 01.00
 */
public class IndexValidatorTest extends BaseTest {

	@Before
	public void before() {
		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath());
		FileUtilities.deleteFile(baseIndexDirectory, 1);
	}

	@After
	public void after() {
		File baseIndexDirectory = new File(indexContext.getIndexDirectoryPath());
		FileUtilities.deleteFile(baseIndexDirectory, 1);
	}

	@Test
	public void validate() throws Exception {
		IIndexValidator indexValidator = ApplicationContextManager.getBean(IIndexValidator.class);
		indexValidator.validate();
		// There should be one mail sent because there are no indexes created

		String serverIndexDirectoryPath = getServerIndexDirectoryPath(indexContext);
		File indexDirectory = createIndex(new File(serverIndexDirectoryPath));
		indexValidator.validate();
		// There should be no mail sent because there is an index generated

		Directory directory = FSDirectory.open(new File(serverIndexDirectoryPath));
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		indexValidator.validate();
		// There should be no mail sent because the index is locked, i.e. being generated
		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		// Delete one file in the index and there should be an exception
		File[] indexFiles = indexDirectory.listFiles();
		for (File indexFile : indexFiles) {
			if (indexFile.getName().contains("segments")) {
				FileUtilities.deleteFile(indexFile, 1);
			}
		}
		indexValidator.validate();
		// There should be a mail sent because the index is corrupt

		FileUtilities.deleteFile(indexDirectory, 1);
	}

}
