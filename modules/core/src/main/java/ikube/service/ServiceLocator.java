package ikube.service;

import ikube.toolkit.UriUtilities;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

public class ServiceLocator {

	private static Logger LOGGER = Logger.getLogger(ServiceLocator.class);

	public static <T> T getService(Class<T> klass, String protocol, String host, int port, String path, String nameSpace, String serviceName) {
		try {
			String url = UriUtilities.buildUri(protocol, host, port, path);
			return getService(klass, url, nameSpace, serviceName);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return null;
	}

	public static <T> T getService(Class<T> klass, String url, String nameSpace, String serviceName) {
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
