package ikube.service;

import javax.xml.ws.Endpoint;

/**
 * This interface is for publishing the web service. The {@link Endpoint} class will bind the service to a port, nice and easy. The
 * implementation could be a bean post processor that looks for the web service annotations and publishes the services dynamically rather
 * than adding more properties to the startup properties.
 * 
 * @author Michael Couck
 * @since 11.08.10
 * @version 01.00
 */
public interface IWebServicePublisher {
	
	void publish();
	
}
