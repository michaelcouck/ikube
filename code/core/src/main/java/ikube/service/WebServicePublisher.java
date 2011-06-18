package ikube.service;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.toolkit.GeneralUtilities;
import ikube.toolkit.Logging;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;

import javax.jws.WebService;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.util.ReflectionUtils;

/**
 * @see IWebServicePublisher
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class WebServicePublisher implements IWebServicePublisher {

	private Logger logger;
	private transient final IClusterManager clusterManager;

	public WebServicePublisher(final IClusterManager clusterManager) {
		logger = Logger.getLogger(this.getClass());
		this.clusterManager = clusterManager;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		try {
			Annotation[] annotations = bean.getClass().getAnnotations();
			for (Annotation annotation : annotations) {
				if (WebService.class.isAssignableFrom(annotation.getClass())) {
					// Publish the web service
					int retryCount = 0;

					Field publishedPortField = ReflectionUtils.findField(bean.getClass(), "publishedPort");
					publishedPortField.setAccessible(Boolean.TRUE);
					int port = (Integer) ReflectionUtils.getField(publishedPortField, bean);

					Field publishedPathField = ReflectionUtils.findField(bean.getClass(), "publishedPath");
					publishedPathField.setAccessible(Boolean.TRUE);
					String path = ReflectionUtils.getField(publishedPathField, bean).toString();

					do {
						URL url = null;
						try {
							String host = InetAddress.getLocalHost().getHostAddress();

							port = GeneralUtilities.findFirstOpenPort(port);

							url = new URL("http", host, port, path);

							Endpoint endpoint = Endpoint.publish(url.toString(), bean);
							Binding binding = endpoint.getBinding();

							logger.info("Published web service to : " + url);
							String message = Logging.getString("Endpoint : ", endpoint, "binding : ", binding, "implementor : ", bean);
							logger.info(message);

							Server server = clusterManager.getServer();
							server.getWebServiceUrls().add(url.toString());
							// Publish the server to the cluster with the new urls
							clusterManager.set(Server.class.getName(), server.getId(), server);
							Thread.sleep(1000);
							break;
						} catch (Exception e) {
							String message = Logging.getString("Exception publishing web service : ", url, bean, e.getMessage());
							logger.info(message);
							if (logger.isDebugEnabled()) {
								logger.error("", e);
							}
							port++;
						}
					} while (++retryCount < IConstants.MAX_RETRY_WEB_SERVICE_PUBLISHER);
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Exception publishing web service : " + bean, e);
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// Do nothing here
		return bean;
	}

}