package ikube.integration;

import ikube.toolkit.UriUtilities;
import ikube.web.servlet.SearchServlet;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * This class starts several Tomcat servers for the cluster integration test.
 * 
 * @author Michael Couck
 * @since 13.03.2011
 * @version 01.00
 */
public class IntegrationCluster {

	private static final Logger LOGGER = Logger.getLogger(IntegrationCluster.class);

	private static final String contextPath = "/";
	private static final Integer[] ports = { 9010 /* , 9020, 9030 */};
	private final String appBase = "./ikube";

	private final String baseDir = "/code/war/target";

	@Test
	public void main() throws Exception {
		String osName = System.getProperty("os.name");
		LOGGER.info("Operating system : " + osName);
		if (!osName.toLowerCase().contains("server")) {
			return;
		}
		List<Process> processes = new ArrayList<Process>();
		for (Integer port : ports) {
			// startClusterServer(port, processes);
			startServer(port);
		}
		Thread.sleep(1000 * 60);
		for (Process process : processes) {
			try {
				LOGGER.info("Stopping server : " + process);
				process.destroy();
			} catch (Exception e) {
				LOGGER.error("Exception stopping process : " + process, e);
			}
		}
	}

	/**
	 * This method starts a single server.
	 * 
	 * @param port
	 *            the port to start the server on
	 */
	private void startServer(Integer port) {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(port);
		tomcat.setBaseDir(".");
		tomcat.getHost().setAppBase("/");
		StandardServer server = (StandardServer) tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);
		try {
			String servletName = SearchServlet.class.getSimpleName();
			Context context = tomcat.addWebapp("/", "/");
			Tomcat.addServlet(context, servletName, SearchServlet.class.getName());
			context.addServletMapping("/" + servletName, servletName);
			tomcat.start();
			// tomcat.getServer().await();
		} catch (Exception e) {
			LOGGER.error("Exception starting Tomcat embedded : ", e);
		}
	}

	@SuppressWarnings("unused")
	private List<Process> startClusterServer(int port, List<Process> processes) throws Exception {
		Map<String, String> environment = System.getenv();
		String classpath = System.getProperty("java.class.path");
		String baseDir = new File(new File("."), this.baseDir).getAbsolutePath();
		String[] command = { "javaw", "-cp", classpath, IntegrationCluster.class.getCanonicalName(), baseDir, appBase,
				Integer.toString(port) };
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(new File("."));
		processBuilder.redirectErrorStream(Boolean.TRUE);
		Map<String, String> targetEnvironment = processBuilder.environment();
		targetEnvironment.putAll(environment);
		Process process = processBuilder.start();
		processes.add(process);
		addLogStreamReader(process);
		return processes;
	}

	private void addLogStreamReader(final Process process) {
		new Thread(new Runnable() {
			public void run() {
				InputStream inputStream = process.getInputStream();
				Reader reader = new InputStreamReader(inputStream);
				char[] chars = new char[1024];
				try {
					int read = -1;
					while ((read = reader.read(chars)) > -1) {
						StringBuilder builder = new StringBuilder();
						builder.append(process.hashCode());
						builder.append(" : ");
						builder.append(chars, 0, read);
						System.out.println(UriUtilities.stripCarriageReturn(builder.toString()));
					}
				} catch (Exception e) {
					LOGGER.error("Exception reading log stream from server : " + process, e);
				}
			}
		}).start();
	}

	public static void main(String[] args) {
		LOGGER.warn("Args : " + Arrays.asList(args));
		LOGGER.warn("Base directory : " + new File(args[0]).getAbsolutePath());
		LOGGER.warn("Application base : " + new File(args[1]).getAbsolutePath());

		int port = Integer.parseInt(args[2]);
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(port);
		tomcat.setBaseDir(args[0]);
		tomcat.getHost().setAppBase(args[1]);
		StandardServer server = (StandardServer) tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);
		try {
			String servletName = SearchServlet.class.getSimpleName();
			Context context = tomcat.addWebapp(contextPath, args[1]);
			Tomcat.addServlet(context, servletName, SearchServlet.class.getName());
			context.addServletMapping("/" + servletName, servletName);
			tomcat.start();
			// tomcat.getServer().await();
		} catch (Exception e) {
			LOGGER.error("Exception starting Tomcat embedded : ", e);
		}
	}

}
