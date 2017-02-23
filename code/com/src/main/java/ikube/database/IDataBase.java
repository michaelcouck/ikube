package ikube.database;

import java.util.List;
import java.util.Map;

/**
 * Interface for database objects. Database objects support access to the persistent store. Implementations
 * of this class then act as the generic data access object for all entities, avoiding boiler place specific code
 * for entities which is so popular now and essentially just a pre-determined place to put the database access.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-04-2009
 */
public interface IDataBase {

    /**
     * This method counts the number of entities in the database.
     *
     * @param klass the class of entities to count
     * @return the number of rows in the database
     */
    <T> Long count(final Class<T> klass);

    /**
     * This method removed the specified entity from the database.
     *
     * @param <T>    the type of object that will be removed
     * @param object the object that must be removed
     * @return the removed object, i.e. a refresh
     */
    <T> T remove(final T object);

    /**
     * Executed a delete statement in the entity manager and returns the number of
     * rows that were affected by the statement, the number of records that were deleted.
     *
     * @param sql the update/delete statement to execute on the database
     * @return the number of rows that were affected
     */
    int remove(final String sql);

    /**
     * Removes the object defined by the type and the id.
     *
     * @param <T>      the type of object that will be removed
     * @param klass    the class of object to remove
     * @param objectId the id of the object to remove
     * @return the removed object, or null if no such object is found to remove
     */
    <T> T remove(final Class<T> klass, final Long objectId);

    /**
     * Removes the batch of objects in the parameter list. Depending on the underlying implementation,
     * this method should be executed in a transaction. As such the commit of the transaction will then
     * flush the removes to the database. So the statements will be batched up and sent in one shot
     * to the database.
     *
     * @param <T>   the type of object to batch remove
     * @param batch the list of objects to batch remove
     */
    <T> void removeBatch(final List<T> batch);

    /**
     * Persists an object in the database.
     *
     * @param object the object to be persisted
     * @return the refreshed object from the database, typically this object will have the unique id
     * filled in by the database
     */
    <T> T persist(final T object);

    /**
     * Persists a batch of objects, i.e. the list passed in the parameter list. As with the
     * {@link IDataBase#removeBatch(List)} method this will run in a transaction, and the commit of
     * the transaction will send the statements to the database, depending on the underlying Jpa
     * implementation.
     *
     * @param <T>  the type of entities to batch persist
     * @param list the list of entities to batch persist
     */
    <T> void persistBatch(final List<T> list);

    /**
     * Merges the object parameter with the object from the database. In the case where a
     * primitive field is changed in the object and this change needs to be persisted the merge
     * will update the database with the new value.
     *
     * @param object the object to be merged or updated in the database
     * @return the refreshed object from the database, typically this will be exactly
     * the same as the object to be merged
     */
    <T> T merge(final T object);

    /**
     * Merges the entities in a batch. Running in a transaction, the underlying implementation
     * will batch up the statements and send them to the database when the transaction is committed.
     *
     * @param <T>   the type of entities to batch merge
     * @param batch the list of entities to batch merge
     */
    <T> void mergeBatch(final List<T> batch);

    /**
     * Finds an object by the id field only. This method will be very expensive as each object in
     * the database is iterated through and the id field found by reflection and compared to the parameter.
     * Also only the first object will be returned. This method assumes that every object in the database has a
     * database scope unique id which will almost never be the case.
     *
     * @param <T>      the type of object to find
     * @param objectId the id of the object
     * @return the first object with the or or null if no such object can be found
     */
    <T> T find(final Long objectId);

    /**
     * Access the object of a particular class with the id.
     *
     * @param <T>      the type of object
     * @param klass    the class of the object
     * @param objectId the id of the object
     * @return the object or null if there is no such object or a runtime exception if there is more
     * than one result, meaning of course that there is a constraint violation, like unique indexes etc.
     */
    <T> T find(final Class<T> klass, final Long objectId);

    /**
     * Selects all the objects in the database that conform to the class type, limited by the max results parameter.
     *
     * @param klass      the class of object to select
     * @param startIndex the first result in the result set, used for paging
     * @param endIndex   the last object in the results, i.e. the index of the last result
     * @return the list of objects from the database specified by the class type
     */
    <T> List<T> find(final Class<T> klass, final int startIndex, final int endIndex);

    /**
     * This method will find the class type specified in the database, and then sort by multiple fields,
     * in the order that they were specified. the direction of sort is boolean for ascending and false for descending.
     *
     * @param <T>             the type to expect
     * @param klass           the class to select from the database
     * @param fieldsToSortOn  the fields to sort on, in the order of the sort requirements, i.e. first
     *                        on start time then on end time for example
     * @param directionOfSort the direction of the sort for each field. For example sort ascending on the
     *                        start time and descending on the end time
     * @param firstResult     the first result in the set, i.e. skip the first n results
     * @param maxResults      and the size of the result set
     * @return the list of sorted entities from the database
     */
    <T> List<T> find(final Class<T> klass, final String[] fieldsToSortOn, final Boolean[] directionOfSort, final int firstResult, final int maxResults);

    /**
     * Selects a single object based on the sql and the parameters. The combination of parameters should
     * result in a single unique entity being returned from the database, otherwise a non unique exception
     * should be thrown.
     *
     * @param <T>        the type of entity expected from the query
     * @param klass      the class of entity expected from the query
     * @param sql        the sql statement to select the entity
     * @param parameters the parameters for the query, must result in a unique result
     * @return the entity that returned from the query and parameters. This could also result in
     * an entity not found exception and a non unique result exception
     */
    <T> T find(final Class<T> klass, final String sql, final Map<String, Object> parameters);

    /**
     * Selects a list of entities from the database based on the sql and the parameters. The results
     * will be limited by the starting position and the maximum results parameters. This method is therefore
     * suited to paging results. Note that the sql should have an order by clause as the underlying implementation
     * will not guarantee the order of the results, the side effect of this is uncertain.
     *
     * @param <T>           the type of entity expected
     * @param klass         the class of entity expected
     * @param sql           the sql to select the entities from the database, should contain an order by clause
     * @param parameters    the parameters to narrow the results
     * @param startPosition the first result position
     * @param maxResults    the maximum results to return
     * @return the list of results determined by the sql, parameters and the limiting parameters
     */
    <T> List<T> find(final Class<T> klass, final String sql, final Map<String, Object> parameters, final int startPosition, final int maxResults);

    /**
     * This method will execute ad-hoc sql, for select, on the database, based on JpaQl like sql. The
     * result from the query depends on the query, for example if there are multiple sums and averages in
     * the query then it will be an array of numbers, but if the query is something like 'select sum(s.count) from
     * Search as s where s.indexName = :indexName', then the result will be a single number. There can only be a
     * single result from this query.
     *
     * @param sql    the JpaQl to execute on the database
     * @param names  the names of the parameters
     * @param values the values of the parameters to narrow the results
     * @return a single result from the query, either an array of object or a single object
     */
    <T> T execute(final String sql, final String[] names, final Object[] values);

    /**
     * This method does a query on the entities using the class type and the parameters are n array format,
     * for the names of the fields to use in the predicate and the values for those fields in the predicate.
     *
     * @param <T>    the type to return
     * @param klass  the class of the return type
     * @param sql    the sql to execute on the database, note that this is a named query, i.e. must be defined
     *               for the JPA implementation
     * @param names  the names of the fields to use as filter in the query
     * @param values the values of the fields to use for the filter in the query
     * @return the entity that matches the fields and values, or null if no such entity exists
     */
    <T> T find(final Class<T> klass, final String sql, final String[] names, final Object[] values);

    /**
     * This method will execute a named query on the database filtered by the parameters which will narrow
     * the results and return a list with of the specified type, starting at the position specified and with a
     * maximum results of that specified.
     *
     * @param <T>           the type to return
     * @param klass         the class of the return type
     * @param sql           the sql to execute on the database, note that this is a named query, i.e. must be defined
     *                      for the JPA implementation
     * @param names         the names of the fields to use as filter in the query
     * @param values        the values of the fields to use for the filter in the query
     * @param startPosition the first entity in the result, in the case that this list os sorted by the id for example
     *                      then this will be the next page in the
     *                      results, if not then the results are defined by the JPA implementation
     * @param maxResults    the maximum results to return from the database
     * @return the list of entities in the database that match the names and values for the fields
     */
    <T> List<T> find(final Class<T> klass, final String sql, final String[] names, final Object[] values, final int startPosition, final int maxResults);

    /**
     * This method will return the first entity that satisfies the query.
     *
     * @param klass            the class type expected
     * @param fieldsToFilterOn the fields in the target entity to use for the selection/filtering
     * @param valuesToFilterOn the values in the target entity to use in the selection/filtering
     * @return the first entity that satisfies the query parameters or null if no such entity exists
     */
    <T> T find(final Class<T> klass, final String[] fieldsToFilterOn, final Object[] valuesToFilterOn);

    /**
     * This method will create a dynamic criteria query based on the field names and the values passed to the
     * query. Note that this query is not type safe and the compiler will not correct and discrepancies in the
     * query prior to execution live.
     *
     * @param <T>              the type of class expected to be returned
     * @param klass            the class type expected
     * @param fieldsToFilterOn the fields in the target entity to use for the selection/filtering
     * @param valuesToFilterOn the values in the target entity to use in the selection/filtering
     * @param firstResult      the first result in the collection, i.e. the starting position in the results
     * @param maxResults       the maximum results to return in the collection
     * @return the resultant collection, can be empty, based on the fields and values specified for the selection
     */
    <T> List<T> find(final Class<T> klass, final String[] fieldsToFilterOn, final Object[] valuesToFilterOn, final int firstResult, final int maxResults);

    /**
     * This method will refresh the entity from the database, essentially getting the changes in the case where
     * it was updated out side of the transaction, i.e. in another thread.
     *
     * @param t the entity from the database to refresh with the latest data
     * @return the refreshed entity
     */
    <T> T refresh(final T t);

}