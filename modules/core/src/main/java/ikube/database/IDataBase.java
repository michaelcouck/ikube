package ikube.database;

import ikube.model.Lock;

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
	 * @param t
	 * @return
	 */
	public <T> T remove(T t);

	/**
	 * Removes an object from the database. The entity is completely deleted.
	 *
	 * @param klass
	 *            the class of the object to be removed
	 * @param id
	 *            the id of the object to be deleted, cannot be null
	 * @return returns the object deleted
	 */
	public <T> T remove(Class<T> klass, Long id);

	/**
	 * Persists an object in the database.
	 *
	 * @param object
	 *            the object to be persisted
	 * @return the refreshed object from the database, typically this object will have the unique id filled in by the database
	 */
	public <T> T persist(T object);

	/**
	 * Merges the object parameter with the object from the database. In the case where a primitive field is changed in the object and this
	 * change needs to be persisted the merge will update the database with the new value.
	 *
	 * @param object
	 *            the object to be merged or updated in the database
	 * @return the refreshed object from the database, typically this will be exactly the same as the object to be merged
	 */
	public <T> T merge(T object);

	/**
	 * Finds an object by it's unique id.
	 *
	 * @param klass
	 *            the class of the object to find
	 * @param id
	 *            the id of the object to find
	 * @return the object with the specified unique id or null if no such object exists
	 */
	public <T> T find(Class<T> klass, Long id);

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
	public <T> T find(Class<T> klass, Map<String, Object> parameters, boolean unique);

	/**
	 * Selects all the objects in the database that conform to the class type, limited by the max results parameter.
	 *
	 * @param klass
	 *            the class of object to select
	 * @param firstResult
	 *            the first result in the result set, used for paging
	 * @param maxResults
	 *            the maximum results to return in the list
	 * @return the list of objects from the database specified by the class type
	 */
	public <T> List<T> find(Class<T> klass, int firstResult, int maxResults);

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
	 * @param firstResult
	 *            the index of the first result in the result set
	 * @param maxResults
	 *            the maximum results to return
	 * @return <T> the list of objects that conform to the narrowing parameters
	 */
	public <T> List<T> find(Class<T> klass, Map<String, Object> parameters, int firstResult, int maxResults);

	public Lock lock(Class<?> klass);

	public void release(Lock lock);

}