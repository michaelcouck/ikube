package ikube.service;

import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.toolkit.GeneralUtilities;
import ikube.toolkit.Logging;

import java.net.InetAddress;
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
	private transient List<String> protocols;
	private transient List<Integer> ports;
	private transient List<String> paths;
	private transient List<Object> implementors;
	private transient final IClusterManager clusterManager;

	public WebServicePublisher(final IClusterManager clusterManager) {
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
			while (true && port < Short.MAX_VALUE) {
				try {
					host = InetAddress.getLocalHost().getHostAddress();
					port = GeneralUtilities.findFirstOpenPort(port);
					URL url = new URL(protocol, host, port, path);
					logger.info("Publishing web service to : " + url);
					Endpoint endpoint = Endpoint.publish(url.toString(), implementor);
					Binding binding = endpoint.getBinding();
					server.getWebServiceUrls().add(url.toString());
					String message = Logging.getString("Endpoint : ", endpoint, "binding : ", binding, "implementor : ", implementor,
							"on address : ", url.toString());
					logger.info(message);
					break;
				} catch (Exception e) {
					logger.info("Port busy : " + protocol + ", " + host + ", " + port + ", " + path + ", " + implementor);
					port++;
				}
			}
		}
		clusterManager.set(Server.class.getName(), server.getId(), server);
	}

	public void setProtocols(final List<String> protocols) {
		this.protocols = protocols;
	}

	public void setPorts(final List<Integer> ports) {
		this.ports = ports;
	}

	public void setPaths(final List<String> paths) {
		this.paths = paths;
	}

	@Override
	public void setImplementors(final List<Object> implementors) {
		this.implementors = implementors;
	}

}
