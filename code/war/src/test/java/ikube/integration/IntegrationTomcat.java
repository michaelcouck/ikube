package ikube.integration;

import org.junit.Test;

/**
 * This class starts several Tomcat servers for the cluster integration test.
 * 
 * @author Michael Couck
 * @since 13.03.2011
 * @version 01.00
 */
public class IntegrationTomcat extends Integration {

	@Test
	public void main() throws Exception {
		if (!isServer()) {
			return;
		}
		Thread.sleep((long) (Math.random() * 10));
		// ApplicationContextManager.getApplicationContext();
		// Tomcat tomcat = new Tomcat();
		// tomcat.setPort(GeneralUtilities.findFirstOpenPort(9000));
		// tomcat.setBaseDir(".");
		// tomcat.getHost().setAppBase("/");
		// StandardServer server = (StandardServer) tomcat.getServer();
		// AprLifecycleListener listener = new AprLifecycleListener();
		// server.addLifecycleListener(listener);
		// try {
		// String servletName = SearchServlet.class.getSimpleName();
		// Context context = tomcat.addWebapp("/", "/");
		// Tomcat.addServlet(context, servletName, SearchServlet.class.getName());
		// context.addServletMapping("/" + servletName, servletName);
		// tomcat.start();
		// // tomcat.getServer().await();
		// } catch (Exception e) {
		// LOGGER.error("Exception starting Tomcat embedded : ", e);
		// }
		// Thread.sleep(1000 * 60 * 60 * 1);
		// validateIndexes();
	}

}
