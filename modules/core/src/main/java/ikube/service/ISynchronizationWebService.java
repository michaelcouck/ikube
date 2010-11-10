package ikube.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = ISynchronizationWebService.NAME, targetNamespace = ISynchronizationWebService.TARGET_NAMESPACE, serviceName = ISynchronizationWebService.SERVICE_NAME)
public interface ISynchronizationWebService {

	public String NAME = "synchronization";
	public String SERVICE_NAME = "synchronization";
	public String TARGET_NAMESPACE = "http://ikube.synchronization/";

	public Boolean wantsFile(String baseDirectory, String latestDirectory, String serverDirectory, String file);

	public Boolean writeIndexFile(String baseDirectory, String latestDirectory, String serverDirectory, String file, byte[] bytes);

}
