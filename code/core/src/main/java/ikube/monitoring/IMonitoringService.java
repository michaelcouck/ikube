package ikube.monitoring;

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

	public String NAME = "monitor";
	public String SERVICE = "monitor";
	public String NAMESPACE = "http://ikube.monitor/";

	public String PUBLISHED_PATH = "/" + IMonitoringService.class.getName().replace(".", "/") + "?wsdl";

	public String[] getIndexNames();

	public String[] getIndexContextNames();

	public String[] getIndexFieldNames(String indexName);
	
	public String[] getIndexableFieldNames(String indexableName);

}
