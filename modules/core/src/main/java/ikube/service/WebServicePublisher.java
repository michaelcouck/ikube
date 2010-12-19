package ikube.service;

import ikube.logging.Logging;
import ikube.toolkit.FileUtilities;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class WebServicePublisher implements IWebServicePublisher {

	private Logger logger;
	private List<String> protocols;
	private List<String> hosts;
	private List<Integer> ports;
	private List<String> paths;
	private List<Object> implementors;

	public WebServicePublisher() {
		logger = Logger.getLogger(this.getClass());
	}

	public void publish() {
		if (hosts == null || implementors == null || hosts.size() != implementors.size()) {
			logger.warn("Addresses and implementors for web service not the same, please check the configuration : ");
			return;
		}
		for (int i = 0; i < hosts.size(); i++) {
			String protocol = protocols.get(i);
			String host = hosts.get(i);
			Integer port = ports.get(i);
			String path = paths.get(i);
			Object implementor = implementors.get(i);
			try {
				URL url = new URL(protocol, host, port, path);

				while (urlInUse(url)) {
					port = new Integer(++port);
					url = new URL(protocol, host, port, path);
				}

				logger.info("Publishing web service to : " + url);
				Endpoint endpoint = Endpoint.publish(url.toString(), implementor);
				Binding binding = endpoint.getBinding();
				String message = Logging.getString("Endpoint : ", endpoint, ", binding : ", binding, ", implementor : ", implementor,
						", on address : ", url.toString());
				logger.info(message);
			} catch (Exception e) {
				logger.error("Exception publishing web service : " + protocol + ", " + host + ", " + port + ", " + path + ", "
						+ implementor, e);
			}
		}
	}

	protected boolean urlInUse(URL url) {
		try {
			logger.info("Checking url : " + url);
			InputStream inputStream = url.openStream();
			String content = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
			logger.info("Url data : " + content);
			return Boolean.TRUE;
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	public void setProtocols(List<String> protocols) {
		this.protocols = protocols;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	public void setPorts(List<Integer> ports) {
		this.ports = ports;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	@Override
	public void setImplementors(List<Object> implementors) {
		this.implementors = implementors;
	}

}
