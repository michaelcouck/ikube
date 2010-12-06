package ikube.service;

import ikube.aspect.IMonitor;
import ikube.toolkit.SerializationUtilities;

import javax.ejb.Remote;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Remote(IMonitorWebService.class)
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = IMonitorWebService.NAME, targetNamespace = IMonitorWebService.TARGET_NAMESPACE, serviceName = IMonitorWebService.SERVICE_NAME)
public class MonitorWebService implements IMonitorWebService {

	private Logger logger = Logger.getLogger(this.getClass());
	private IMonitor monitor;

	@Override
	public String monitor() {
		String serialized = SerializationUtilities.serialize(monitor.getExecutions());
		logger.debug("Monitor : " + serialized);
		return serialized;
	}

	public void setMonitor(IMonitor monitor) {
		this.monitor = monitor;
	}

}
