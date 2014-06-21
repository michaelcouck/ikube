package ikube.web;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;

/**
 * This is the bootstrap class for stand alone. The libraries that are required are defined in
 * the manifest of the core jar, and the manifest points to the {@code /lib} folder directly under
 * the jar file.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-01-2011
 */
public final class Ikube {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ikube.class);

	static Server SERVER = new Server();

	public static void main(final String[] args) {
		if (args[0].equals("start")) {
			startServer(Integer.parseInt(args[1]));
		} else if (args[0].equals("stop")) {
			stopServer();
		}
	}

	private static void startServer(final int port) {
		try {
			if (SERVER.isRunning() || SERVER.isStarted() || SERVER.isStarting()) {
				LOGGER.info("Server running : " + SERVER.isRunning() + ", " + SERVER.isStarted() + ", " + SERVER.isStarting());
				return;
			}
			String ip = InetAddress.getLocalHost().getHostAddress();
			Connector connector = new SelectChannelConnector();
			connector.setPort(port);
			connector.setHost(ip);
			SERVER.addConnector(connector);
			LOGGER.info("Got connector : " + connector);

			WebAppContext webAppContext = new WebAppContext();
			webAppContext.setContextPath(IConstants.SEP + IConstants.IKUBE);
			// This is path to .war OR TO expanded, existing webapp; WILL FIND web.xml and parse it
			File warFile = FileUtilities.findFileRecursively(new File("."), 2, "ikube\\.war");
			LOGGER.info("War file : " + warFile + ", " + webAppContext);
			webAppContext.setWar(warFile.getAbsolutePath());
			SERVER.setHandler(webAppContext);
			SERVER.setStopAtShutdown(true);

			SERVER.start();
			LOGGER.info("Started server : " + SERVER);
		} catch (final Exception e) {
			LOGGER.error("Exception starting embedded server : ", e);
		}
	}

	private static void stopServer() {
		try {
			SERVER.stop();
			SERVER.destroy();
			LOGGER.info("Stopped server : ");
		} catch (final Exception e) {
			LOGGER.error("Exception stopping embedded server : ", e);
		}
	}

}