package ikube.service;

import ikube.model.IndexContext;

import java.util.HashMap;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = IMonitoringService.NAME, targetNamespace = IMonitoringService.TARGET_NAMESPACE, serviceName = IMonitoringService.SERVICE_NAME)
public interface IMonitoringService {

	public String NAME = "monitor";
	public String SERVICE_NAME = "monitor";
	public String TARGET_NAMESPACE = "http://ikube.monitor/";

	public HashMap<String, IndexContext> getIndexContexts();

}
