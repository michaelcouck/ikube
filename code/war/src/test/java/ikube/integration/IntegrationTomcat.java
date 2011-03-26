package ikube.integration;

import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.GeneralUtilities;
import ikube.web.servlet.SearchServlet;

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
public class IntegrationTomcat extends Integration {

	private static final Logger LOGGER = Logger.getLogger(IntegrationTomcat.class);

	@Test
	public void main() throws Exception {
		String osName = System.getProperty("os.name");
		LOGGER.info("Operating system : " + osName);
		if (!osName.toLowerCase().contains("server")) {
			// return;
		}
		ApplicationContextManager.getApplicationContext();
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(GeneralUtilities.findFirstOpenPort(9000));
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
		Thread.sleep(1000 * 60 * 60 * 1);
		validateIndexes();
	}

}
