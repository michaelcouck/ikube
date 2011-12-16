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
	public Boolean execute(final IndexContext<?> indexContext) {
		long actionId = 0;
		try {
			actionId = start(indexContext, "");
			delete(dataBase, ikube.model.Action.class, new String[] { "startTime" }, new boolean[] { true }, (int) IConstants.MAX_ACTIONS);
		} finally {
			stop(indexContext, actionId);
		}
		return Boolean.TRUE;
	}

	protected void delete(final IDataBase dataBase, final Class<?> klass, final String[] fieldsToSortOn, final boolean[] directionOfSort,
			final int maxSize) {
		int doubleMaxSize = maxSize * 2;
		List<?> entities = dataBase.find(klass, fieldsToSortOn, directionOfSort, 0, doubleMaxSize);
		if (entities.size() <= maxSize) {
			return;
		}
		for (int i = 0; i < entities.size() - maxSize; i++) {
			Object entity = entities.get(i);
			dataBase.remove(entity);
		}
		delete(dataBase, klass, fieldsToSortOn, directionOfSort, maxSize);
	}

}