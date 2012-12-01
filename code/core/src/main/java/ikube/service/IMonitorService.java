package ikube.service;

import ikube.model.Attribute;
import ikube.model.IndexContext;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;

import java.util.Map;

import javax.persistence.Column;

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
	 * This method will return all the field names for a particular index. These field names are the names of the fields in Lucene, i.e. the
	 * mapping between the indexable and their corresponding names in Lucene. These fields include any child indexables and theor field
	 * names too. For example if there is a {@link IndexableTable} which has child {@link IndexableColumn}s then the field names returned
	 * will be all the fields in the composite table and the columns.
	 * 
	 * @param indexName the name of the index
	 * @return all the field names defined for this index, this will include all the fields in all the indexables
	 */
	String[] getIndexFieldNames(final String indexName);

	/**
	 * Access to the index contexts keyed by their names.
	 * 
	 * @return the index contexts in the system, this includes the contexts that are defined in the Spring configuration and the index
	 *         contexts in the database
	 */
	@SuppressWarnings("rawtypes")
	Map<String, IndexContext> getIndexContexts();

	/**
	 * This method will access a particular index context, by the name. Names of index contexts are unique in the cluster.
	 * 
	 * @param indexContextName the name of the index context
	 * @return the index context with the name or null if there is no such context with that name
	 */
	@SuppressWarnings("rawtypes")
	IndexContext getIndexContext(final String indexContextName);

	/**
	 * This method returns the names of the fields in the index class specified. Field names are properties in an indexable that can be
	 * persisted. Consequently they all have the {@link Column} annotation.
	 * 
	 * @param indexableClass the class of indexable
	 * @return the fields in the indexable that define the {@link Column} annotation but not the children indexables
	 */
	String[] getFieldNames(final Class<?> indexableClass);

	/**
	 * This method will return the field descriptions. Descriptions for fields are defined by using the {@link Attribute} annotation.
	 * Descriptions are typically human readable and could be used in validation messages and so on.
	 * 
	 * @param indexableClass the indexable class to get all the field descriptions for
	 * @return the list of field descriptions that are defined in the indexable in the order that they are found using reflections. Note
	 *         that this will be the same order for all the methods that return fields using reflection
	 */
	String[] getFieldDescriptions(final Class<?> indexableClass);

	/**
	 * This method will get the properties defined in the configuration files.
	 * 
	 * @return the map of properties, the key is the path to the file and the value is the contents of the file
	 */
	Map<String, String> getProperties();

	/**
	 * This method will set the contents of the properties files.
	 * 
	 * @param filesAndProperties the key is the file path and the value of the map is the contents of the file to write
	 */
	void setProperties(final Map<String, String> filesAndProperties);

}