package ikube.integration;

import ikube.IConstants;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.GeneralUtilities;
import ikube.web.servlet.SearchServlet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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

	private String webApp = "webapp";
	private String contextPath = IConstants.SEP + IConstants.IKUBE;

	@Test
	@Override
	public void main() throws Exception {
		if (!isServer()) {
			// return;
		}
		ApplicationContextManager.getApplicationContext();
		String webAppContextFilePath = getWebAppContextFilePath();
		Server server = new Server(GeneralUtilities.findFirstOpenPort(9000));
		Context root = new Context(server, contextPath, Context.SESSIONS);
		server.setHandler(new WebAppContext(webAppContextFilePath, contextPath));
		root.addServlet(new ServletHolder(new SearchServlet()), IConstants.SEP + SearchServlet.class.getSimpleName());

		server.start();
		Thread.sleep(1000 * 60 * 1);
		validateIndexes();
		// TODO Stress test the servlets in each alive server
	}

	private String getWebAppContextFilePath() throws MalformedURLException {
		File file = FileUtilities.findFile(new File("."), webApp);
		URL webAppBaseDirectoryUrl = file.toURI().toURL();
		return webAppBaseDirectoryUrl.toExternalForm();
	}

}
