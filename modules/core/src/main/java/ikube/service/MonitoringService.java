package ikube.service;

import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.HashMap;

import javax.ejb.Remote;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@Remote(IMonitoringService.class)
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = IMonitoringService.NAME, targetNamespace = IMonitoringService.TARGET_NAMESPACE, serviceName = IMonitoringService.SERVICE_NAME)
public class MonitoringService implements IMonitoringService {

	@Override
	public HashMap<String, IndexContext> getIndexContexts() {
		return (HashMap<String, IndexContext>) ApplicationContextManager.getBeans(IndexContext.class);
	}

}
