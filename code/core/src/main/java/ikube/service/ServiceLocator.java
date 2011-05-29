package ikube.service;

import ikube.toolkit.UriUtilities;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public final class ServiceLocator {

	private static final Logger LOGGER = Logger.getLogger(ServiceLocator.class);

	private ServiceLocator() {
	}

	/**
	 * TODO Document me.
	 * 
	 * @param <T>
	 * @param klass
	 * @param protocol
	 * @param host
	 * @param port
	 * @param path
	 * @param nameSpace
	 * @param serviceName
	 * @return
	 */
	public static <T> T getService(final Class<T> klass, final String protocol, final String host, final int port, final String path,
			final String nameSpace, final String serviceName) {
		try {
			String url = UriUtilities.buildUri(protocol, host, port, path);
			return getService(klass, url, nameSpace, serviceName);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return null;
	}

	/**
	 * TODO Document me.
	 * 
	 * @param <T>
	 * @param klass
	 * @param url
	 * @param nameSpace
	 * @param serviceName
	 * @return
	 */
	public static <T> T getService(final Class<T> klass, final String url, final String nameSpace, final String serviceName) {
		try {
			URL wsdlURL = new URL(url);
			QName qName = new QName(nameSpace, serviceName);
			Service service = Service.create(wsdlURL, qName);
			return service.getPort(klass);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return null;
	}

}
