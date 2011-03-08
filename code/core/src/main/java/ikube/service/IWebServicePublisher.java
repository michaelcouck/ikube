package ikube.service;

import java.util.List;

import javax.xml.ws.Endpoint;

/**
 * This interface is for publishing the web service. The {@link Endpoint} class will bind the service to a port, nice and easy.
 * 
 * @author Michael Couck
 * @since 11.08.10
 * @version 01.00
 */
public interface IWebServicePublisher {

	/**
	 * Publishes the service on a port, and with a particular name/address.
	 */
	void publish();

	void setPorts(List<Integer> ports);

	void setPaths(List<String> paths);

	void setProtocols(List<String> protocols);

	/**
	 * Sets the implementations that would be published.
	 * 
	 * @param implementors
	 *            the implementations that are instantiated already, to be bound to the addresses
	 */
	void setImplementors(List<Object> implementors);

}
