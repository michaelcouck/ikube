package ikube.service;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.toolkit.GeneralUtilities;
import ikube.toolkit.Logging;

import java.net.InetAddress;
import java.net.URL;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @see IWebServicePublisher
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class WebServicePublisher implements IWebServicePublisher {

	private Logger logger;
	@Autowired
	private IClusterManager clusterManager;
	@Autowired
	private List<IPublishable> publishables;

	public WebServicePublisher() {
		logger = Logger.getLogger(this.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	public void publish() throws BeansException {
		// Publish the web service
		for (IPublishable publishable : publishables) {
			int retryCount = 0;
			int port = publishable.getPort();
			String path = publishable.getPath();
			do {
				URL url = null;
				try {
					String host = InetAddress.getLocalHost().getHostAddress();
					port = GeneralUtilities.findFirstOpenPort(port);
					url = new URL("http", host, port, path);
					Endpoint endpoint = Endpoint.publish(url.toString(), publishable);
					Binding binding = endpoint.getBinding();
					logger.info("Published web service to : " + url);
					String message = Logging.getString("Endpoint : ", endpoint, "binding : ", binding, "implementor : ", publishable);
					logger.info(message);
					if (url.toString().contains(ISearcherWebService.class.getSimpleName())) {
						clusterManager.getServer().setSearchWebServiceUrl(url.toString());
					}
					break;
				} catch (Exception e) {
					logger.warn("Exception publishing web service : " + e.getMessage());
					port++;
				}
			} while (++retryCount < IConstants.MAX_RETRY_WEB_SERVICE_PUBLISHER);
		}
	}

}