package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;

import java.util.Iterator;
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
		delete(dataBase, ikube.model.Action.class, new String[] { "startTime" }, new Boolean[] { true },
				IConstants.RESET_DELETE_BATCH_SIZE, (int) IConstants.MAX_ACTIONS / 4);
		delete(dataBase, ikube.model.Snapshot.class, new String[] {}, new Boolean[] {}, IConstants.RESET_DELETE_BATCH_SIZE,
				(int) IConstants.MAX_SNAPSHOTS / 4);
		return Boolean.TRUE;
	}

	protected void delete(final IDataBase dataBase, final Class<?> klass, final String[] fieldsToSortOn, final Boolean[] directionOfSort,
			final int batchSize, final int toRemain) {
		do {
			int count = dataBase.count(klass).intValue();
			int calculatedBatchSize = Math.max(Math.min(count - toRemain, batchSize), 0);
			if (calculatedBatchSize <= 0) {
				break;
			}
			List<?> entities = dataBase.find(klass, fieldsToSortOn, directionOfSort, 0, calculatedBatchSize);
			Iterator<?> iterator = entities.iterator();
			while (iterator.hasNext()) {
				dataBase.remove(iterator.next());
			}
		} while (true);
	}

}