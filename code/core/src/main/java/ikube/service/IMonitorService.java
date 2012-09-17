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

	/**
	 * Accesses the index names defined in the configuration. The index names are the names given to the indexes by the user. For example in
	 * the case of the Geospatial index, the name of the index is geospatial.
	 * 
	 * @return the names of the indexes
	 */
	String[] getIndexNames();

	/**
	 * This method will return all the field names for a particular index.
	 * 
	 * @param indexName
	 *            the name of the index
	 * @return all the field names defined for this index, this will include all the fields in all the indexables
	 */
	String[] getIndexFieldNames(final String indexName);

	/**
	 * Access to the index contexts keyed by their names.
	 * 
	 * @return the index contexts in the system
	 */
	@SuppressWarnings("rawtypes")
	Map<String, IndexContext> getIndexContexts();
	
	@SuppressWarnings("rawtypes")
	IndexContext getIndexContext(final String indexContextName);
	
	String[] getFieldNames(final Class<?> indexableClass);

	public abstract String[] getFieldDescriptions(final Class<?> indexableClass);

}