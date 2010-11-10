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
	public void publish();

	/**
	 * Set the addresses that are to be used to publish the web services.
	 *
	 * @param addresses
	 *            the addresses where to bind the services. For example an address could be
	 *            'http://localhost:81/search-war-1.0/ISearchService'
	 */
	public void setAddresses(List<String> addresses);

	/**
	 * Sets the implementations that would be published.
	 *
	 * @param implementors
	 *            the implementations that are instantiated already, to be bound to the addresses
	 */
	public void setImplementors(List<Object> implementors);

}
