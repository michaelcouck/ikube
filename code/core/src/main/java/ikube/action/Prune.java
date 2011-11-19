package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;

import java.util.List;
import java.util.Map;

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
		try {
			start(indexContext, "");
			delete(dataBase, ikube.model.Action.class, new String[] { "startTime" }, new boolean[] { true }, (int) IConstants.MAX_ACTIONS);
		} finally {
			stop(indexContext, "");
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

	@Deprecated
	protected void delete(final IDataBase dataBase, final Class<?> klass, final String sql, final Map<String, Object> parameters) {
		try {
			List<?> list = dataBase.find(klass, sql, parameters, 0, IConstants.RESET_DELETE_BATCH_SIZE);
			dataBase.removeBatch(list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}