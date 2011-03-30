package ikube.integration;

import ikube.IConstants;
import ikube.integration.strategy.JspStrategy;
import ikube.integration.strategy.ServletStrategy;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.GeneralUtilities;
import ikube.web.servlet.SearchServlet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * This test is for integration testing in a cluster in a server.
 * 
 * @author Michael Couck
 * @since 26.03.11
 * @version 01.00
 */
public class IntegrationJetty extends Integration {

	/** The port to start from. */
	private int port = 9000;
	private String webApp = "webapp";
	private String ikubeWar = "ikube-war";
	private String contextPath = IConstants.SEP + IConstants.IKUBE;

	@Test
	@Override
	public void main() throws Exception {
		if (!isServer()) {
			return;
		}
		Thread.sleep((long) (Math.random() * 10));
		String webAppContextFilePath = getWebAppContextFilePath();
		int port = GeneralUtilities.findFirstOpenPort(this.port);

		while (true && port < Short.MAX_VALUE) {
			try {
				logger.info("Starting server on port : " + port + ",  in directory : " + webAppContextFilePath);
				Server server = new Server(port);
				Context root = new Context(server, contextPath, Context.SESSIONS);
				server.setHandler(new WebAppContext(webAppContextFilePath, contextPath));
				root.addServlet(new ServletHolder(new SearchServlet()), IConstants.SEP + SearchServlet.class.getSimpleName());
				server.start();
			} catch (Exception e) {
				logger.info("Port occupied? We'll try another one : " + port);
				port++;
			}
		}

		Thread.sleep((long) (1000 * 60 * 60));
		// waitToFinish();

		validateIndexes();

		// Test all the jsps
		new JspStrategy(contextPath, port).perform();

		// Stress test the servlet a little
		int iterations = 1000;
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		String[] indexNames = indexContexts.keySet().toArray(new String[indexContexts.keySet().size()]);
		new ServletStrategy(contextPath, port, iterations, indexNames).perform();
	}

	private String getWebAppContextFilePath() throws MalformedURLException {
		// First try the target build directory
		File file = FileUtilities.findFile(new File("."), ikubeWar);
		if (file == null) {
			// Then the project webapp directory
			file = FileUtilities.findFile(new File("."), webApp);
		}
		URL webAppBaseDirectoryUrl = file.toURI().toURL();
		return webAppBaseDirectoryUrl.toExternalForm();
	}

}
