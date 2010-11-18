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
				for (IndexContext indexContext : indexContexts.values()) {
					// Get the servers
					Set<Server> servers = clusterManager.getServers();
					list.add(servers);
					// Get the index files
					File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
					list.add(latestIndexDirectory.getAbsolutePath());
					File[] serverIndexDirectories = latestIndexDirectory.listFiles();
					if (serverIndexDirectories != null && serverIndexDirectories.length > 0) {
						for (File serverIndexDirectory : serverIndexDirectories) {
							list.add(serverIndexDirectory.getAbsolutePath());
							if (serverIndexDirectory != null) {
								File[] indexFiles = serverIndexDirectory.listFiles();
								if (indexFiles != null) {
									for (File indexFile : indexFiles) {
										list.add(indexFile.getAbsolutePath());
									}
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
