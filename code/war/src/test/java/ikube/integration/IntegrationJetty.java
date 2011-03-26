package ikube.integration;

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

public class IntegrationJetty extends Integration {

	private String webApp = "webapp";
	private String contextPath = "/ikube";

	@Test
	@Override
	public void main() throws Exception {
		if (!isServer()) {
			return;
		}
		ApplicationContextManager.getApplicationContext();
		String webAppContextFilePath = getWebAppContextFilePath();
		Server server = new Server(GeneralUtilities.findFirstOpenPort(9000));
		Context root = new Context(server, contextPath, Context.SESSIONS);
		server.setHandler(new WebAppContext(webAppContextFilePath, contextPath));
		root.addServlet(new ServletHolder(new SearchServlet()), "/" + SearchServlet.class.getSimpleName());

		server.start();
		Thread.sleep(1000 * 60 * 60);
		validateIndexes();
		// TODO Stress test the servlets in each alive server
	}

	private String getWebAppContextFilePath() throws MalformedURLException {
		File file = FileUtilities.findFile(new File("."), webApp);
		URL webAppBaseDirectoryUrl = file.toURI().toURL();
		return webAppBaseDirectoryUrl.toExternalForm();
	}

}
