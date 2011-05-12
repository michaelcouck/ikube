package ikube.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = IMonitoringService.NAME, targetNamespace = IMonitoringService.NAMESPACE, serviceName = IMonitoringService.SERVICE)
public interface IMonitoringService {

	String NAME = "monitor";
	String SERVICE = "monitor";
	String NAMESPACE = "http://ikube.monitor/";

	String PUBLISHED_PATH = "/" + IMonitoringService.class.getName().replace(".", "/") + "?wsdl";

	String[] getIndexNames();

	String[] getIndexContextNames();

	String[] getIndexFieldNames(String indexName);
	
	String[] getIndexableFieldNames(String indexableName);

}
