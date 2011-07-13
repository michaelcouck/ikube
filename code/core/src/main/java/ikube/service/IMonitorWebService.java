package ikube.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * This class provides access to the index contexts, the names of the fields in the index and so on. Also names of the indexes that are
 * defined in the configuration.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@WebService(name = IMonitorWebService.NAME, targetNamespace = IMonitorWebService.NAMESPACE, serviceName = IMonitorWebService.SERVICE)
public interface IMonitorWebService {

	String NAME = "monitor";
	String SERVICE = "monitor";
	String NAMESPACE = "http://ikube.monitor/";

	int PUBLISHED_PORT = 8082;
	String PUBLISHED_PATH = "/" + IMonitorWebService.class.getName().replace(".", "/") + "?wsdl";

	/**
	 * Accesses the index names defined in the configuration. The index names are the names given to the indexes by the user. For example in
	 * the case of the Geospatial index, the name of the index is geospatial.
	 * 
	 * @return the names of the indexes
	 */
	@WebMethod
	@WebResult(name = "indexNames")
	String[] getIndexNames();

	/**
	 * This method will return the indexable names for a particular index.
	 * 
	 * @param indexName
	 *            the name of the index
	 * @return the indexable names for this index
	 */
	@WebMethod
	@WebResult(name = "indexableNames")
	String[] getIndexableNames(@WebParam(name = "indexName") final String indexName);

	/**
	 * This method returns the names of the index contexts in the Spring context, i.e. the names of the beans that are defined in the Spring
	 * configuration.
	 * 
	 * @return the names of the Spring bean names for the index contexts
	 */
	@WebMethod
	@WebResult(name = "indexContextNames")
	String[] getIndexContextNames();

	/**
	 * This method will return all the field names for a particular index.
	 * 
	 * @param indexName
	 *            the name of the index
	 * @return all the field names defined for this index, this will include all the fields in all the indexables
	 */
	@WebMethod
	@WebResult(name = "indexFieldNames")
	String[] getIndexFieldNames(@WebParam(name = "indexName") final String indexName);

	/**
	 * This method will return the field names for a particular indexable.
	 * 
	 * @param indexableName
	 *            the name of the indexable to get the field names for
	 * @return the field names defined for this particular indexable
	 */
	@WebMethod
	@WebResult(name = "indexableFieldNames")
	String[] getIndexableFieldNames(@WebParam(name = "indexableName") final String indexableName);

	/**
	 * TODO Comment me!
	 * 
	 * @param indexName
	 * @return
	 */
	@WebMethod
	@WebResult(name = "indexSize")
	long getIndexSize(@WebParam(name = "indexName") final String indexName);

	/**
	 * TODO Comment me!
	 * 
	 * @param indexName
	 * @return
	 */
	@WebMethod
	@WebResult(name = "indexDocuments")
	int getIndexDocuments(@WebParam(name = "indexName") final String indexName);

}