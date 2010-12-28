package ikube.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = IMonitoringService.NAME, targetNamespace = IMonitoringService.NAMESPACE, serviceName = IMonitoringService.SERVICE)
public interface IMonitoringService {

	public String NAME = "monitor";
	public String SERVICE = "monitor";
	public String NAMESPACE = "http://ikube.monitor/";

	public String[] getIndexNames();
	
	public String[] getIndexContextNames();
	
	public String[] getFieldNames(String indexName);

}
