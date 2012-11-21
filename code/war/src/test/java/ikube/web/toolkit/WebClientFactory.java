package ikube.web.toolkit;

import java.security.GeneralSecurityException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;

/**
 * This class opens the WebClient and sets the default handlers and so on. In the case that a client wants to open a web client with
 * different properties then the WebClient should be closed and
 * {@link WebClientFactory#getWebClient(BrowserVersion, boolean, boolean, boolean)} called with the appropriate parameters.
 * 
 * The default {@link WebClientFactory#getWebClient()} returns the default WebClient with Firefox as the version and JavaScript and CSS not
 * enabled. Note that JavaScript and CSS are memory intensive and should not be used in combination with long or deep crawls.
 * 
 * @author Michael Couck
 * @since 25.09.10
 * @version 01.00
 */
public class WebClientFactory {

	private static Logger LOGGER = LoggerFactory.getLogger(WebClientFactory.class);
	private static WebClient WEB_CLIENT;

	/**
	 * This listener just overrides the default logging for obsolete 'JavaScript' text as it is very verbose.
	 * 
	 * @author Michael Couck
	 * @since 25.09.10
	 * @version 01.00
	 */
	public static class IncorrectnessListenerImpl implements IncorrectnessListener {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());
		private final IncorrectnessListener incorrectnessListener;

		public IncorrectnessListenerImpl(IncorrectnessListener incorrectnessListener) {
			this.incorrectnessListener = incorrectnessListener;
		}

		@Override
		public void notify(String paramString, Object paramObject) {
			if (paramString.contains("Obsolete")) {
				return;
			}
			logger.warn("String : {}, object: {}", paramString, paramObject);
			incorrectnessListener.notify(paramString, paramObject);
		}
	}

	/**
	 * Instantiates and or returns the default web client. Please refer to class JavaDoc for more information on the default client. This
	 * will be FireFox 3.6 with no JavaScript or Css enabled.
	 * 
	 * @return the currently alive web client, if the web client is not active then it will be instantiated
	 */
	public static synchronized WebClient getWebClient() {
		if (WEB_CLIENT == null) {
			// The default client will not run the JavaScript nor will it check for CSS errors and will not throw
			// exceptions
			WEB_CLIENT = WebClientFactory.getWebClient(BrowserVersion.FIREFOX_3_6, false, false, false);
		}
		return WEB_CLIENT;
	}

	/**
	 * Instantiates the web client with the desired properties if the client is not already instantiated. If the web client is instantiated
	 * and not null then it just returns the currently alive client.
	 * 
	 * @param browserVersion the browser version, possible values are Firefox, IE... Have a look at the {@link BrowserVersion} for possible
	 *            values
	 * @param javaScriptEnabled whether the JavaScript in the pages will be executed. Note that this is memory and time intensive
	 * @param cssEnabled whether the CSS is enables. This will generally cause a lot of debug logging
	 * @param throwExceptionOnScriptError whether to throw exceptions on JavaScript errors
	 * @return
	 */
	public static synchronized WebClient getWebClient(BrowserVersion browserVersion, boolean javaScriptEnabled, boolean cssEnabled,
			boolean throwExceptionOnScriptError) {
		if (WEB_CLIENT == null) {
			WEB_CLIENT = new WebClient(browserVersion);
			WEB_CLIENT.setJavaScriptEnabled(javaScriptEnabled);
			WEB_CLIENT.setCssEnabled(cssEnabled);
			WEB_CLIENT.setThrowExceptionOnScriptError(throwExceptionOnScriptError);

			WEB_CLIENT.setTimeout(60000);
			WEB_CLIENT.setJavaScriptTimeout(60000);
			WEB_CLIENT.setAppletEnabled(Boolean.FALSE);
			WEB_CLIENT.setThrowExceptionOnFailingStatusCode(Boolean.FALSE);
			WEB_CLIENT.setIncorrectnessListener(new IncorrectnessListenerImpl(WEB_CLIENT.getIncorrectnessListener()));
			WEB_CLIENT.setPopupBlockerEnabled(Boolean.TRUE);
			// WEB_CLIENT.setCookieManager(new CookieManager());
			try {
				WEB_CLIENT.setUseInsecureSSL(Boolean.TRUE);
			} catch (GeneralSecurityException e) {
				LOGGER.error("Security exception setting the SSL property : ", e);
			}

			WEB_CLIENT.setAlertHandler(new AlertHandler() {
				@Override
				public void handleAlert(Page paramPage, String paramString) {
					try {
						paramPage.cleanUp();
					} catch (Exception e) {
						LOGGER.error("Exception cleaning up the page : ", e);
					}
				}
			});
		}
		return WEB_CLIENT;
	}

	/**
	 * Closes the web client and releases all the resources, nulling the default static web client as well making it ready to change browser
	 * versions.
	 */
	public static synchronized void closeWebClient() {
		if (WEB_CLIENT == null) {
			LOGGER.warn("Web client null : ");
			return;
		}
		long totalMemoryBefore = Runtime.getRuntime().totalMemory();
		LOGGER.info("Closing the web client : {}, {}", WEB_CLIENT, totalMemoryBefore);
		WEB_CLIENT.getCache().clear();
		List<WebWindow> webWindows = WEB_CLIENT.getWebWindows();
		for (WebWindow webWindow : webWindows) {
			try {
				for (int i = 0; i < webWindow.getJobManager().getJobCount(); i++) {
					webWindow.getJobManager().stopJob(i);
				}
				webWindow.getJobManager().removeAllJobs();
				webWindow.getJobManager().shutdown();
			} catch (Exception e) {
				LOGGER.error("Exception shutting down the JavaScript jobs : ", e);
			}
		}
		WEB_CLIENT.closeAllWindows();
		WEB_CLIENT = null;
		long totalMemoryAfter = Runtime.getRuntime().totalMemory();
		LOGGER.info("Closed the web client : {}, before : {}, after : {}, released : {}", new Object[] { WEB_CLIENT, totalMemoryBefore,
				totalMemoryAfter, (totalMemoryBefore - totalMemoryAfter) });
	}

}