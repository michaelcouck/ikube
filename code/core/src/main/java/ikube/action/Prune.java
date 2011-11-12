package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;

import java.util.HashMap;
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
			Long count = dataBase.execute(Long.class, ikube.model.Action.SELECT_FROM_ACTIONS_COUNT);
			while (count != null && count > IConstants.MAX_ACTIONS / 2) {
				logger.info("Pruning : " + count);
				Map<String, Object> parameters = new HashMap<String, Object>();
				delete(dataBase, ikube.model.Action.class, ikube.model.Action.SELECT_FROM_ACTIONS, parameters);
				count = dataBase.execute(Long.class, ikube.model.Action.SELECT_FROM_ACTIONS_COUNT);
			}
		} finally {
			clusterManager.stopWorking(getClass().getSimpleName(), indexContext.getIndexName(), "");
		}
		return Boolean.TRUE;
	}

	protected void delete(final IDataBase dataBase, final Class<?> klass, final String sql, final Map<String, Object> parameters) {
		try {
			List<?> list = dataBase.find(klass, sql, parameters, 0, IConstants.RESET_DELETE_BATCH_SIZE);
			dataBase.removeBatch(list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}