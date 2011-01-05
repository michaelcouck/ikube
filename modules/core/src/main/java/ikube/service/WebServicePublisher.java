package ikube.service;

import ikube.cluster.IClusterManager;
import ikube.logging.Logging;
import ikube.model.Server;
import ikube.toolkit.FileUtilities;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class WebServicePublisher implements IWebServicePublisher {

	private Logger logger;
	private List<String> protocols;
	private List<Integer> ports;
	private List<String> paths;
	private List<Object> implementors;
	private IClusterManager clusterManager;

	public WebServicePublisher(IClusterManager clusterManager) {
		logger = Logger.getLogger(this.getClass());
		this.clusterManager = clusterManager;
	}

	public void publish() {
		assert protocols.size() == ports.size() && ports.size() == paths.size() && paths.size() == implementors.size();
		Server server = clusterManager.getServer();
		for (int i = 0; i < implementors.size(); i++) {
			String protocol = protocols.get(i);
			Integer port = ports.get(i);
			String path = paths.get(i);
			String host = null;
			Object implementor = implementors.get(i);
			try {
				host = InetAddress.getLocalHost().getHostAddress();
				URL url = new URL(protocol, host, port, path);
				while (urlInUse(url)) {
					port = new Integer(++port);
					url = new URL(protocol, host, port, path);
				}
				logger.info("Publishing web service to : " + url);
				Endpoint endpoint = Endpoint.publish(url.toString(), implementor);
				Binding binding = endpoint.getBinding();
				server.getWebServiceUrls().add(url.toString());
				String message = Logging.getString("Endpoint : ", endpoint, "binding : ", binding, "implementor : ", implementor,
						"on address : ", url.toString());
				logger.info(message);
			} catch (Exception e) {
				logger.error("Exception publishing web service : " + protocol + ", " + host + ", " + port + ", " + path + ", "
						+ implementor, e);
			}
		}
		clusterManager.set(Server.class, server.getId(), server);
	}

	protected boolean urlInUse(URL url) {
		try {
			logger.info("Checking url : " + url);
			InputStream inputStream = url.openStream();
			String content = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
			logger.info("Url data : " + StringUtils.trimAllWhitespace(content));
			return Boolean.TRUE;
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	public void setProtocols(List<String> protocols) {
		this.protocols = protocols;
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
