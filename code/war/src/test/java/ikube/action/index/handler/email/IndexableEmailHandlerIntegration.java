package ikube.action.index.handler.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableEmail;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.IMailer;
import ikube.toolkit.Mailer;
import ikube.toolkit.ThreadUtilities;

import java.net.InetAddress;
import java.util.concurrent.ForkJoinTask;

import org.apache.lucene.index.IndexWriter;
import org.junit.Test;

/**
 * Test for the mail handler. Put a mail on the account because once the mail is read it is marked as read and will not read it again. Of course this must be
 * fixed.
 * 
 * @author Big Boy Bruno Barin (B.B.B.B)
 * @since this century
 * @version 102235366.2215.3688.744RC112556
 */
public class IndexableEmailHandlerIntegration extends IntegrationTest {

	@SuppressWarnings("rawtypes")
	private IndexContext indexContext;

	@Test
	public void handle() throws Exception {
		indexContext = monitorService.getIndexContext("indexContext");
		IMailer mailer = ApplicationContextManager.getBean(Mailer.class);
		mailer.sendMail("MailhandlerTest Subject", "Mail handler test mail body");
		String ip = InetAddress.getLocalHost().getHostAddress();
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
		indexContext.setIndexWriters(indexWriter);

		IndexableEmail indexableEmail = ApplicationContextManager.getBean(IndexableEmail.class);
		IndexableEmailHandler indexableEmailHandler = ApplicationContextManager.getBean(IndexableEmailHandler.class);

		ForkJoinTask<?> forkJoinTask = indexableEmailHandler.handleIndexableForked(indexContext, indexableEmail);
		ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexContext.getThreads(), forkJoinTask);
		ThreadUtilities.sleep(15000);
		ThreadUtilities.cancellForkJoinPool(indexContext.getName());

		assertNotNull("The index writer should still be available : ", indexContext.getIndexWriters());
		assertEquals("There should only be one index writer : ", 1, indexContext.getIndexWriters().length);
		assertTrue("Should be some documents, we just put a mail in the account : ", indexWriter.numDocs() > 0);
	}

}
