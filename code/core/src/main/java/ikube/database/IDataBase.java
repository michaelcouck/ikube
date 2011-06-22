package ikube.database;

import java.util.List;
import java.util.Map;

/**
 * Interface for database objects. Database objects support access to the persistent store.
 * 
 * @author Michael Couck
 * @since 20.04.09
 * @version 01.00
 */
public interface IDataBase {

	/**
	 * This method removed the specified entity from the database.
	 * 
	 * @param <T>
	 *            the type of object that will be removed
	 * @param object
	 *            the object that must be removed
	 * @return the removed object, i.e. a refresh
	 */
	<T> T remove(T object);

	int remove(String sql);

	/**
	 * Removes the object defined by the type and the id.
	 * 
	 * @param <T>
	 *            the type of object that will be removed
	 * @param klass
	 *            the class of object to remove
	 * @param objectId
	 *            the id of the object to remove
	 * @return the removed object, or null if no such object is found to remove
	 */
	<T> T remove(Class<T> klass, Long objectId);

	<T> void removeBatch(List<T> batch);

	/**
	 * Persists an object in the database.
	 * 
	 * @param object
	 *            the object to be persisted
	 * @return the refreshed object from the database, typically this object will have the unique id filled in by the database
	 */
	<T> T persist(T object);

	<T> void persistBatch(List<T> list);

	/**
	 * Merges the object parameter with the object from the database. In the case where a primitive field is changed in the object and this
	 * change needs to be persisted the merge will update the database with the new value.
	 * 
	 * @param object
	 *            the object to be merged or updated in the database
	 * @return the refreshed object from the database, typically this will be exactly the same as the object to be merged
	 */
	<T> T merge(T object);

	<T> void mergeBatch(List<T> batch);

	/**
	 * Finds an object by the if field only. This method will be very expensive as each object in the database is iterated through and the
	 * id field found by reflection and compared to the parameter. Also only the first object will be returned. This method assumes that
	 * every object in the database has a database scope unique id.
	 * 
	 * @param <T>
	 *            the type of object to find
	 * @param objectId
	 *            the id of the object
	 * @return the first object with the or or null if no such object can be found
	 */
	<T> T find(Long objectId);

	/**
	 * Access the object of a particular class with the id.
	 * 
	 * @param <T>
	 *            the type of object
	 * @param klass
	 *            the class of the object
	 * @param objectId
	 *            the id of the object
	 * @return the object or null if there is no such object or a runtime exception if there is more than one result, meaning of course that
	 *         there is a constraint violation, like unique indexes etc.
	 */
	<T> T find(Class<T> klass, Long objectId);

	/**
	 * Finds a single object in the database restricted by the parameter values. Note that this method will return the first entity that
	 * matches the parameter list which could be random, based on the underlying implementation of the object relational layer. Unless of
	 * course the unique flag is set, in which case if there is a non unique result then null will be returned, and some error logging.
	 * 
	 * @param <T>
	 *            the object from the database
	 * @param klass
	 * @param parameters
	 *            the parameters to narrow the select by
	 * @param unique
	 *            whether the result is unique according the the parameters
	 * @return the object resulting from the select. In the case where there is more than one object returned or none the entity manager
	 *         will throw an exception
	 */
	// <T> T find(Class<T> klass, Map<String, Object> parameters, boolean unique);

	/**
	 * Selects all the objects in the database that conform to the class type, limited by the max results parameter.
	 * 
	 * @param klass
	 *            the class of object to select
	 * @param startIndex
	 *            the first result in the result set, used for paging
	 * @param endIndex
	 *            the last object in the results, i.e. the index of the last result
	 * @return the list of objects from the database specified by the class type
	 */
	<T> List<T> find(Class<T> klass, int startIndex, int endIndex);

	<T> T find(Class<T> klass, String sql, Map<String, Object> parameters);

	<T> List<T> find(Class<T> klass, String sql, Map<String, Object> parameters, int startPosition, int maxResults);

	/**
	 * Finds a list of objects in the database that conform to the parameters specified in the method signature. The results are also
	 * limited to a max results and the starting position in the result set allows paging functionality. Note that for paging the select
	 * needs to have an order-by clause. This is because the first index in the result set needs to be at the same position. Also in the
	 * case where there are objects added to the database between paging the paging results will not be accurate.
	 * 
	 * @param <T>
	 *            the type of class for the results
	 * @param klass
	 *            the class of object to select from the database
	 * @param queryName
	 *            the name of the query. Note that these queries are in the persistent classes them selves
	 * @param parameters
	 *            the narrowing parameters for the select
	 * @param startIndex
	 *            the index of the first result in the result set
	 * @param endIndex
	 *            the last object in the results, i.e. the index of the last result
	 * @return <T> the list of objects that conform to the narrowing parameters
	 */
	// <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int startIndex, int endIndex);

}