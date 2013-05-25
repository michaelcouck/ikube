package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;

import java.util.List;

/**
 * This class will prune any data that needs to be cleaned in the database from time to time.
 * 
 * @author Michael Couck
 * @since 29.09.2011
 * @version 01.00
 */
public class Prune extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalExecute(final IndexContext<?> indexContext) {
		String[] fieldsToSortOn = new String[] { "id" };
		Boolean[] directionOfSort = new Boolean[] { true };
		delete(dataBase, ikube.model.Action.class, fieldsToSortOn, directionOfSort, IConstants.MAX_ACTIONS);
		delete(dataBase, ikube.model.Snapshot.class, fieldsToSortOn, directionOfSort, IConstants.MAX_SNAPSHOTS);
		delete(dataBase, ikube.model.Server.class, fieldsToSortOn, directionOfSort, IConstants.MAX_SERVERS);
		return Boolean.TRUE;
	}

	protected void delete(final IDataBase dataBase, final Class<?> klass, final String[] fieldsToSortOn, final Boolean[] directionOfSort,
			final long toRemain) {
		int batchSize = (int) toRemain / 4;
		int count = dataBase.count(klass).intValue();
		while (count > toRemain) {
			logger.info("Count : " + count + ", " + toRemain + ", " + batchSize);
			List<?> entities = dataBase.find(klass, fieldsToSortOn, directionOfSort, 0, batchSize);
			dataBase.removeBatch(entities);
			count = dataBase.count(klass).intValue();
		}
	}

}