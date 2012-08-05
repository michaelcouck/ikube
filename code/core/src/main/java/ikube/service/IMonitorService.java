package ikube.service;

import ikube.model.IndexContext;

import java.util.Map;

/**
 * This class provides access to the index contexts, the names of the fields in the index and so on. Also names of the indexes that are
 * defined in the configuration.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public interface IMonitorService {

	String NAME = "monitor";
	String SERVICE = "monitor";
	String NAMESPACE = "http://ikube.monitor/";

	/**
	 * Accesses the index names defined in the configuration. The index names are the names given to the indexes by the user. For example in
	 * the case of the Geospatial index, the name of the index is geospatial.
	 * 
	 * @return the names of the indexes
	 */
	String[] getIndexNames();

	/**
	 * This method will return the indexable names for a particular index.
	 * 
	 * @param indexName
	 *            the name of the index
	 * @return the indexable names for this index
	 */
	String[] getIndexableNames(final String indexName);

	/**
	 * This method returns the names of the index contexts in the Spring context, i.e. the names of the beans that are defined in the Spring
	 * configuration.
	 * 
	 * @return the names of the Spring bean names for the index contexts
	 */
	String[] getIndexContextNames();

	/**
	 * This method will return all the field names for a particular index.
	 * 
	 * @param indexName
	 *            the name of the index
	 * @return all the field names defined for this index, this will include all the fields in all the indexables
	 */
	String[] getIndexFieldNames(final String indexName);

	/**
	 * This method will return the field names for a particular indexable.
	 * 
	 * @param indexableName
	 *            the name of the indexable to get the field names for
	 * @return the field names defined for this particular indexable
	 */
	String[] getIndexableFieldNames(final String indexableName);

	/**
	 * Returns the size of the index on the disk.
	 * 
	 * @param indexName
	 *            the name of the index to get the size for
	 * @return the size of the index
	 */
	long getIndexSize(final String indexName);

	/**
	 * Returns the number of documents in the specified index.
	 * 
	 * @param indexName
	 *            the name of the index to get the document count for
	 * @return the number of documents in the index
	 */
	int getIndexDocuments(final String indexName);

	/**
	 * Access to the index contexts keyed by their names.
	 * 
	 * @return the index contexts in the system
	 */
	@SuppressWarnings("rawtypes")
	Map<String, IndexContext> getIndexContexts();

}