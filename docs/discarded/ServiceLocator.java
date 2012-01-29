package ikube.service;

import ikube.toolkit.UriUtilities;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

/**
 * This class just takes care of the dynamic/boiler plate code for the generation of the clients for web services.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public final class ServiceLocator {

	private static final Logger LOGGER = Logger.getLogger(ServiceLocator.class);

	/**
	 * Singularity.
	 */
	private ServiceLocator() {
		// Documented
	}

	/**
	 * This method will return the web service based on the parameters, building the url with the components.
	 * 
	 * @param <T> the type of service to return
	 * @param klass the class of web service object to return
	 * @param protocol the protocol for the web service
	 * @param host the host machine, or domain where the service is located
	 * @param port the expected port of the web service
	 * @param path the path of the url to the service
	 * @param nameSpace the name space for the web service
	 * @param serviceName and the service name for the web service
	 * @return the web service class at the specified url or null if there is a problem, either the service doesn't exist at the specified
	 *         url or the components were incorrectly specified
	 */
	public static <T> T getService(final Class<T> klass, final String protocol, final String host, final int port, final String path,
			final String nameSpace, final String serviceName) {
		try {
			String url = UriUtilities.buildUri(protocol, host, port, path);
			return getService(klass, url, nameSpace, serviceName);
		} catch (Exception e) {
			LOGGER.error("Exception accessing the web service class : " + klass + ", protocol : " + protocol + ", host : " + host
					+ ", port : " + port + ", path : " + path + ", name space : " + nameSpace + ", service name : " + serviceName, e);
		}
		return null;
	}

	/**
	 * This method will return the web service based on the complete url specified in the parameter list and the service name and name
	 * space.
	 * 
	 * @param <T> the type of service to return
	 * @param klass the class of web service object to return
	 * @param nameSpace the name space for the web service
	 * @param serviceName and the service name for the web service
	 * @return the web service class at the specified url or null if there is a problem, either the service doesn't exist at the specified
	 *         url or the components were incorrectly specified
	 */
	public static <T> T getService(final Class<T> klass, final String url, final String nameSpace, final String serviceName) {
		try {
			URL wsdlURL = new URL(url);
			QName qName = new QName(nameSpace, serviceName);
			Service service = Service.create(wsdlURL, qName);
			return service.getPort(klass);
		} catch (Exception e) {
			LOGGER.error("Exception accessing the web service class : " + klass + ", url : " + url + ", name space : " + nameSpace
					+ ", service name : " + serviceName, e);
		}
		return null;
	}

}
