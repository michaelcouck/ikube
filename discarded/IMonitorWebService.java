package ikube.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = IMonitorWebService.NAME, targetNamespace = IMonitorWebService.TARGET_NAMESPACE, serviceName = IMonitorWebService.SERVICE_NAME)
public interface IMonitorWebService {

	public String NAME = "monitor";
	public String SERVICE_NAME = "monitor";
	public String TARGET_NAMESPACE = "http://ikube.monitor/";

	public String monitor();

}
