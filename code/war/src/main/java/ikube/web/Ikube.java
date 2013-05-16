package ikube.web;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.net.InetAddress;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import org.mortbay.jetty.Server;

/**
 * This is the bootstrap class for stand alone. The libraries that are required are defined in the manifest of the core jar, and the
 * manifest points to the {@code /lib} folder directly under the jar file.
 * 
 * @author Michael Couck
 * @since 23.01.11
 * @version 01.00
 */
public final class Ikube {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ikube.class);

	public static void main(final String[] args) {
		startServer();
	}

	private static final void startServer() {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			Server server = new Server();
			Connector connector = new SelectChannelConnector();
			connector.setPort(8080);
			connector.setHost(ip);
			server.addConnector(connector);

			WebAppContext webAppContext = new WebAppContext();
			webAppContext.setContextPath(IConstants.SEP + IConstants.IKUBE);
			// This is path to .war OR TO expanded, existing webapp; WILL FIND web.xml and parse it
			File warFile = FileUtilities.findFileRecursively(new File("."), "ikube.war");
			webAppContext.setWar(warFile.getAbsolutePath());
			server.setHandler(webAppContext);
			server.setStopAtShutdown(true);

			server.start();
		} catch (Exception e) {
			LOGGER.error("Exception starting embedded server : ", e);
		}
	}

}