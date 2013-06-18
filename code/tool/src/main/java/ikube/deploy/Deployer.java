package ikube.deploy;

import ikube.deploy.action.IAction;
import ikube.deploy.model.Server;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public final class Deployer {

	public static void main(String[] args) {
		ThreadUtilities.initialize();
		List<Future<?>> futures = new ArrayList<Future<?>>();
		// Find the configuration file
		File deployerConfiguration = FileUtilities.findFileRecursively(new File("."), "deployer\\.xml");
		String configLocation = FileUtilities.cleanFilePath(deployerConfiguration.getAbsolutePath());
		ApplicationContext applicationContext = new FileSystemXmlApplicationContext(configLocation);
		Deployer deployer = applicationContext.getBean(Deployer.class);
		for (final Server server : deployer.getServers()) {
			Future<?> future = ThreadUtilities.submitSystem(new Runnable() {
				public void run() {
					for (final IAction action : server.getActions()) {
						action.execute(server);
					}
				}
			});
			if (deployer.isParallel()) {
				futures.add(future);
			} else {
				ThreadUtilities.waitForFuture(future, deployer.getMaxWaitTime());
			}
		}
		if (deployer.isParallel()) {
			ThreadUtilities.waitForFutures(futures, deployer.getMaxWaitTime());
		}
	}

	private boolean parallel;
	private long maxWaitTime = 600000;
	private Collection<Server> servers;

	public boolean isParallel() {
		return parallel;
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	public long getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(long maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public Collection<Server> getServers() {
		return servers;
	}

	public void setServers(Collection<Server> servers) {
		this.servers = servers;
	}

}
