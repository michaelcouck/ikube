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
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ClusterTest {

	static {
		Logging.configure();
	}

	// 3600000
	public static long SLEEP = 600000;
	private static Logger LOGGER = Logger.getLogger(ClusterTest.class);

	@Test
	public void start() throws Exception {
		// Delete all the old index directories
		Map<String, IndexContext> contexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : contexts.values()) {
			File baseIndexDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			FileUtilities.deleteFile(baseIndexDirectory, 1);
		}
	}

	public static void main(String[] args) throws Exception {
		args = new String[] { "serverOne", "serverTwo", "serverThree" };
		final List<Process> processes = new ArrayList<Process>();
		FileUtilities.deleteFiles(new File("."), IConstants.DATABASE_FILE, IConstants.TRANSACTION_FILES);
		Map<String, String> environment = System.getenv();
		String classpath = System.getProperty("java.class.path");
		// "/cluster/spring.xml", "/META-INF/spring.xml"
		String configurationFile = "/META-INF/spring.xml";
		for (final String serverAddress : args) {
			String[] command = { "javaw", "-cp", classpath, ServerRunner.class.getCanonicalName(), serverAddress, configurationFile };
			ProcessBuilder processBuilder = new ProcessBuilder(command);

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
							builder.append(serverAddress);
							builder.append(" : ");
							builder.append(chars, 0, read);
							System.out.println(UriUtilities.stripCarriageReturn(builder.toString()));
						}
					} catch (Exception e) {
						LOGGER.error("", e);
					}
				}
			}).start();
		}

		LOGGER.info("Going to sleep : " + Thread.currentThread().hashCode());
		Thread.sleep(SLEEP);
		LOGGER.info("Waking up : " + Thread.currentThread().hashCode());

		for (Process process : processes) {
			LOGGER.info("Destroying process : " + process);
			process.destroy();
		}

		FileUtilities.deleteFiles(new File("."), IConstants.DATABASE_FILE, IConstants.TRANSACTION_FILES);
		System.exit(0);
	}

}