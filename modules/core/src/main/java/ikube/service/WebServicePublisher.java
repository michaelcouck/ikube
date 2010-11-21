package ikube.service;

import ikube.logging.Logging;

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

	private Logger logger = Logger.getLogger(this.getClass());
	private List<String> addresses;
	private List<Object> implementors;

	public void setAddresses(List<String> addresses) {
		this.addresses = addresses;
	}

	public void setImplementors(List<Object> implementors) {
		this.implementors = implementors;
	}

	public void publish() {
		if (addresses == null || implementors == null || addresses.size() != implementors.size()) {
			logger.warn("Addresses and implementors for web service not the same, please check the configuration : ");
			return;
		}
		int index = 0;
		for (String address : addresses) {
			Object implementor = null;
			try {
				implementor = implementors.get(index);
				Endpoint endpoint = Endpoint.publish(address, implementor);
				Binding binding = endpoint.getBinding();
				String message = Logging.getString("Endpoint : ", endpoint, ", binding : ", binding, ", implementor : ", implementor,
						", on address : ", address);
				logger.info(message);
			} catch (Exception e) {
				logger.error("Exception publishing web service : " + address + ", " + implementor, e);
			}
		}

	}

}
