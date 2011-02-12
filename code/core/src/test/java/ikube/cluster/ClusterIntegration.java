package ikube.cluster;

import ikube.IConstants;
import ikube.logging.Logging;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.UriUtilities;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterIntegration {

	static {
		Logging.configure();
	}

	public static long SLEEP = 1000 * 60 * 60 * 72;
	private static Logger LOGGER = Logger.getLogger(ClusterIntegration.class);

	public static void start() throws Exception {
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
	}

	public static void main(String[] arguments) throws Exception {
		// ClusterIntegration.start();
		String[] servers = new String[] { "ServerOne", "ServerTwo", "ServerThree" };
		String clusterDirectoryPath = "./cluster";
		String classpath = System.getProperty("java.class.path");

		final List<Process> processes = new ArrayList<Process>();
		Map<String, String> environment = System.getenv();

		File clusterDirectory = FileUtilities.getFile(clusterDirectoryPath, Boolean.TRUE);
		FileUtilities.deleteFile(clusterDirectory, 1);

		String[] command = { "javaw", "-cp", classpath, ServerRunner.class.getCanonicalName(), IConstants.SPRING_CONFIGURATION_FILE };

		for (final String serverName : servers) {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			File workingDirectory = FileUtilities.getFile(clusterDirectoryPath + IConstants.SEP + serverName, Boolean.TRUE);
			processBuilder.directory(workingDirectory);

			processBuilder.redirectErrorStream(Boolean.TRUE);

			Map<String, String> targetEnvironment = processBuilder.environment();
			targetEnvironment.putAll(environment);

			final Process process = processBuilder.start();
			processes.add(process);

			new Thread(new Runnable() {
				public void run() {
					InputStream inputStream = process.getInputStream();
					Reader reader = new InputStreamReader(inputStream);
					char[] chars = new char[1024];
					try {
						int read = -1;
						while ((read = reader.read(chars)) > -1) {
							StringBuilder builder = new StringBuilder();
							builder.append(serverName);
							builder.append(" : ");
							builder.append(chars, 0, read);
							System.out.println(UriUtilities.stripCarriageReturn(builder.toString()));
						}
					} catch (Exception e) {
						LOGGER.error("", e);
					}
				}
			}).start();
			Thread.sleep(5000);
		}

		LOGGER.info("Going to sleep : " + Thread.currentThread().hashCode());
		Thread.sleep(SLEEP);
		LOGGER.info("Waking up : " + Thread.currentThread().hashCode());

		for (Process process : processes) {
			LOGGER.info("Destroying process : " + process);
			process.destroy();
		}
	}

}