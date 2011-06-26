package ikube.monitoring;

import ikube.cluster.IClusterManager;
import ikube.index.IndexManager;
import ikube.listener.Event;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.Mailer;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Michael Couck
 * @since 16.01.11
 * @version 01.00
 */
@Deprecated
public class IndexValidator implements IIndexValidator {

	private static final Logger LOGGER = Logger.getLogger(IndexValidator.class);

	public IndexValidator() {
		ListenerManager.addListener(new IListener() {
			@Override
			public void handleNotification(final Event event) {
				if (event.getType().equals(Event.VALIDATION)) {
					validate();
				}
			}
		});
	}

	@Override
	public void validate() {
		// Conditions:
		// 1) There is an index but it is locked, i.e. an index is running
		// 2) There are two indexes and one is locked
		// 3) There is one index, i.e. the current one
		// 4) There are no indexes
		// 5) There are indexes but they are corrupt

		// There must be at least one index being generated, or one index created
		// and one being generated for each index context
		Server server = ApplicationContextManager.getBean(IClusterManager.class).getServer();
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (String name : contexts.keySet()) {
			IndexContext<?> indexContext = contexts.get(name);
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			File baseIndexDirectory = new File(indexDirectoryPath);
			File[] timeIndexDirectories = baseIndexDirectory.listFiles();
			if (timeIndexDirectories == null || timeIndexDirectories.length == 0) {
				String subject = "1 : Ikube no indexes generated for server : " + server.getAddress();
				String body = "No indexes for " + indexContext.getIndexName() + " generated.";
				sendNotification(indexContext, subject, body);
				continue;
			}
			boolean indexCreated = Boolean.FALSE;
			boolean indexGenerated = Boolean.FALSE;
			for (File timeIndexDirectory : timeIndexDirectories) {
				File[] serverIndexDirectories = timeIndexDirectory.listFiles();
				if (serverIndexDirectories == null || serverIndexDirectories.length == 0) {
					String subject = "2 : Ikube no indexes generated for server : " + server.getAddress();
					String body = "No indexes for " + indexContext.getIndexName() + " generated.";
					sendNotification(indexContext, subject, body);
					continue;
				}
				for (File serverIndexDirectory : serverIndexDirectories) {
					Directory directory = null;
					try {
						directory = FSDirectory.open(serverIndexDirectory);
						boolean exists = IndexReader.indexExists(directory);
						boolean locked = IndexWriter.isLocked(directory);
						LOGGER.info("Exists : " + exists + ", locked : " + locked + ", directory : " + serverIndexDirectory);
						if (exists && !locked) {
							indexCreated = Boolean.TRUE;
						}
						if (exists && locked) {
							indexGenerated = Boolean.TRUE;
						}
					} catch (Exception e) {
						LOGGER.error("Exception validating indexes for index context : " + indexContext, e);
						String subject = "3 : Ikube index corrupt : " + server.getAddress();
						String body = "Index " + serverIndexDirectory + " corrupt, index context : " + indexContext.getIndexName();
						sendNotification(indexContext, subject, body);
					} finally {
						try {
							if (directory != null) {
								directory.close();
							}
						} catch (Exception e) {
							LOGGER.error("Exception closing the directory : ", e);
						}
					}
				}
			}
			if (indexCreated || indexGenerated) {
				continue;
			}
			String subject = "4 : Ikube indexes not generated or being generated : " + server.getAddress();
			String body = "No indexes generated or in the process of being generated for index context : " + indexContext.getIndexName();
			sendNotification(indexContext, subject, body);
		}
	}

	protected void sendNotification(final IndexContext<?> indexContext, final String subject, final String body) {
		try {
			Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
			mailer.sendMail(subject, body);
		} catch (Exception e) {
			LOGGER.error("Exception sending mail : ", e);
		}
	}

}