package ikube.service;

import ikube.cluster.IClusterManager;
import ikube.model.Server;
import ikube.toolkit.GeneralUtilities;
import ikube.toolkit.Logging;

import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
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

	@Override
	public void publish() {
		List<String> webServiceUrls = new ArrayList<String>();
		for (int i = 0; i < implementors.size(); i++) {
			String host = null;
			String path = paths.get(i);
			Integer port = ports.get(i);
			String protocol = protocols.get(i);
			Object implementor = implementors.get(i);
			try {
				host = InetAddress.getLocalHost().getHostAddress();
				port = GeneralUtilities.findFirstOpenPort(port);
				URL url = new URL(protocol, host, port, path);
				logger.info("Publishing web service to : " + url);
				Endpoint endpoint = Endpoint.publish(url.toString(), implementor);
				Binding binding = endpoint.getBinding();
				webServiceUrls.add(url.toString());
				String message = Logging.getString("Endpoint : ", endpoint, "binding : ", binding, "implementor : ", implementor,
						"on address : ", url.toString());
				logger.info(message);
			} catch (Exception e) {
				logger.info(
						"Exception publishing web service : " + protocol + ", " + host + ", " + port + ", " + path + ", " + implementor, e);
			}
		}
		Server server = clusterManager.getServer();
		server.getWebServiceUrls().addAll(webServiceUrls);
		// Publish the server to the cluster with the new urls
		clusterManager.set(Server.class.getName(), server.getId(), server);
	}

	@Override
	public void setProtocols(final List<String> protocols) {
		this.protocols = protocols;
	}

	@Override
	public void setPorts(final List<Integer> ports) {
		this.ports = ports;
	}

	@Override
	public void setPaths(final List<String> paths) {
		this.paths = paths;
	}

	@Override
	public void setImplementors(final List<Object> implementors) {
		this.implementors = implementors;
	}

}