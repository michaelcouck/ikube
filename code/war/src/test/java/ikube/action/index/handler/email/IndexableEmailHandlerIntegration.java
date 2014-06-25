package ikube.action.index.handler.email;

import java.net.InetAddress;
import java.util.concurrent.ForkJoinTask;

import org.apache.lucene.index.IndexWriter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.sun.mail.smtp.SMTPSendFailedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.IndexableEmail;
import ikube.toolkit.IMailer;
import ikube.toolkit.ThreadUtilities;

/**
 * Test for the mail handler. Put a mail on the account because once the mail is
 * read it is marked as read and will not read it again. Of course this must be fixed.
 *
 * @author Big Boy Bruno Barin (B.B.B.B)
 * @version 102235366.2215.3688.744RC112556
 * @since this century
 */
@SuppressWarnings({ "UnusedDeclaration", "SpringJavaAutowiringInspection" })
public class IndexableEmailHandlerIntegration extends IntegrationTest {

	@Autowired
	private IMailer mailer;
	@Autowired
	@Qualifier("indexContext")
	private IndexContext indexContext;
	@Autowired
	private IndexableEmail indexableEmail;
	@Autowired
	private IndexableEmailHandler indexableEmailHandler;

	@Test
	public void handle() throws Exception {
		try {
			mailer.sendMail("Mail handler test Subject", "Mail handler test mail body");
			ThreadUtilities.sleep(15000);

			String ip = InetAddress.getLocalHost().getHostAddress();
			IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), ip);
			indexContext.setIndexWriters(indexWriter);

			ForkJoinTask<?> forkJoinTask = indexableEmailHandler.handleIndexableForked(indexContext, indexableEmail);
			ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexContext.getThreads(), forkJoinTask);
			ThreadUtilities.sleep(15000);
			ThreadUtilities.cancelForkJoinPool(indexContext.getName());

			assertNotNull("The index writer should still be available : ", indexContext.getIndexWriters());
			assertEquals("There should only be one index writer : ", 1, indexContext.getIndexWriters().length);
			// This only works if the account limit is not reached!!! Uncomment when there is a
			// mail server setup that can send more than a few thousand mails in a day!!!
			// assertTrue("Should be some documents, we just put a mail in the account : ", indexWriter.numDocs() > 0);
		} catch (SMTPSendFailedException e) {
			if (e.getReturnCode() != 550) {
				throw e;
			}
		}
	}

}
