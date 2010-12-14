package ikube.toolkit;

import ikube.cluster.IClusterManager;
import ikube.listener.IListener;
import ikube.model.Event;
import ikube.model.IndexContext;
import ikube.model.Server;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Reporter implements IListener {

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void handleNotification(Event event) {
		if (event.getType().equals(Event.REPORT)) {
			try {
				List<Object> list = new ArrayList<Object>();
				list.add(InetAddress.getLocalHost().getHostAddress());

				// Get the index contexts
				Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
				IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
				Set<Server> servers = clusterManager.getServers();
				list.add(servers);
				for (IndexContext indexContext : indexContexts.values()) {
					// Get the index files
					File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
					list.add(baseIndexDirectory.getAbsolutePath());
					File[] contextIndexDirectories = baseIndexDirectory.listFiles();
					if (contextIndexDirectories == null) {
						continue;
					}
					for (File contextIndexDirectory : contextIndexDirectories) {
						if (contextIndexDirectory == null) {
							continue;
						}
						list.add(contextIndexDirectory.getAbsolutePath());
						File[] timeIndexDirectories = contextIndexDirectory.listFiles();
						if (timeIndexDirectories == null) {
							continue;
						}
						for (File timeIndexDirectory : timeIndexDirectories) {
							File[] serverIndexDirectories = timeIndexDirectory.listFiles();
							if (serverIndexDirectories == null) {
								continue;
							}
							for (File serverIndexDirectory : serverIndexDirectories) {
								File[] indexFiles = serverIndexDirectory.listFiles();
								if (indexFiles == null) {
									continue;
								}
								for (File indexFile : indexFiles) {
									list.add(indexFile.getAbsolutePath());
								}
							}
						}
					}
				}
				Mailer mailer = ApplicationContextManager.getBean(Mailer.class);
				String xml = SerializationUtilities.serialize(list);
				mailer.sendMail("Ikube report", xml);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}

}
