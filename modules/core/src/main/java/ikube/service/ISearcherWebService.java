package ikube.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = ISearcherWebService.NAME, targetNamespace = ISearcherWebService.TARGET_NAMESPACE, serviceName = ISearcherWebService.SERVICE_NAME)
public interface ISearcherWebService {

	public String NAME = "searcher";
	public String SERVICE_NAME = "searcher";
	public String TARGET_NAMESPACE = "http://ikube.search/";

	public String searchSingle(String indexName, String searchString, String searchField, boolean fragment, int start, int end);

	public String searchMulti(String indexName, String[] searchStrings, String[] searchFields, boolean fragment, int start, int end);

}
