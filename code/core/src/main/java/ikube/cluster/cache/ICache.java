package ikube.cluster.cache;

import ikube.model.IndexableInternet;
import ikube.model.Server;
import ikube.model.Url;

import java.util.List;
import java.util.Map;

import com.hazelcast.core.IMap;

/**
 * This is the cache interface that will be used across the cluster servers. The implementation needs to be clusterable and synchronised,
 * i.e. there must be locking available on the cache in the cluster before modification.
 * 
 * Implementors must have a {@link Map} of {@link IMap} maps that are keyed by name. For example {@link Url} objects need to be published in
 * the cluster. Each map of urls must be keyed on the name of the indexable. When indexing an {@link IndexableInternet} object, the name of
 * this indexable will be 'ikube' for example. Clients in the cluster will then check the cache to see if this url is already crawled. To
 * access the map of urls for this indexable clients will use the name, {@link ICache#get(String, ICriteria, IAction, int)} to get a list of
 * not yet crawled urls. The criteria that is passed to the method will check each url to see if it has been crawled and if the result is
 * true then the cache should add the url to the batch that is returned.
 * 
 * @author Michael Couck
 * @since 15.12.10
 * @version 01.00
 */
public interface ICache {

	/**
	 * This is an interface that can be passed to the get method in the cache that can execute arbitrary logic on the object in the cache as
	 * the cache iterates over the objects.
	 * 
	 * @author Michael Couck
	 */
	interface IAction<T> {
		void execute(T object);
	}

	/**
	 * This criteria interface can be used to determine whether to include the item from the cache and include it in the batch returned by
	 * the {@link ICache#get(String, ICriteria, IAction, int)} method.
	 * 
	 * @author Michael Couck
	 */
	interface ICriteria<T> {
		boolean evaluate(T object);
	}

	/**
	 * Returns the size of the cache for this map. Typically the name of the map will be the name of the class that is stored in that map.
	 * 
	 * @param name
	 *            the name of the map, generally the name of the class, like {@link Server} for example.
	 * @return
	 */
	int size(String name);

	/**
	 * Clears this map of objects in the cache. This action is of course cluster wide so it should not be called until there are no servers
	 * running at all.
	 * 
	 * @param name
	 *            the name of the map to clear
	 */
	void clear(String name);

	/**
	 * Access to one object in the specified map based on the unique id of that object.
	 * 
	 * @param <T>
	 *            the type of object expected to be returned
	 * @param name
	 *            the name of the map
	 * @param id
	 *            the id of the object in the map
	 * @return the object with the specified id
	 */
	<T> T get(String name, Long id);

	/**
	 * Access to the object in the specified map based on a SQL like query. The SQL could be something like (urlString =
	 * 'http://www.ikokoon.eu/'). This could be specific to Hazelcast in fact and could be difficult to replace by another provider.
	 * 
	 * @param <T>
	 *            the type of object expected to be returned
	 * @param name
	 *            the name of the map
	 * @param sql
	 *            the sql predicate to select the object on
	 * @return an object that satisfies the sql predicate. Note that the first object in found that satisfies this query will be returned
	 *         and no unique or duplicate checking will be done. In the case that the objects are unique based on the sql then this should
	 *         be no problem
	 */
	<T> T get(String name, String sql);

	/**
	 * Sets the specified object in the map based on the id. This method also propagates the object throughout the cluster.
	 * 
	 * @param <T>
	 *            the type of object
	 * @param name
	 *            the name of the map
	 * @param id
	 *            the id of the object
	 * @param object
	 *            the object it's self
	 */
	<T> void set(String name, Long id, T object);

	/**
	 * Removes the specified object from the cache/map and throughout the cluster.
	 * 
	 * @param name
	 *            the name of the map
	 * @param id
	 *            the id of the object to be removed
	 */
	void remove(String name, Long id);

	/**
	 * This method gets a batch of objects. If the criteria is specified then the return value from the criteria is used to determine
	 * whether the object will be added to the batch that is returned to the caller.
	 * 
	 * @param <T>
	 *            the type of object to return to the caller
	 * @param name
	 *            the name of the map to access
	 * @param criteria
	 *            the criteria to determine if the object should be added to the batch, can be null
	 * @param action
	 *            the action to execute on the object while iterating over the objects in the map, can be null
	 * @param size
	 *            the maximum size of the batch that should be returned
	 * @return the batch of objects from the cache
	 */
	<T> List<T> get(String name, ICriteria<T> criteria, IAction<T> action, int size);

}