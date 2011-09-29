package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

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
			logger.info("Pruning : " + Thread.currentThread().hashCode());
			IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
			Long count = dataBase.execute(Long.class, ikube.model.Action.SELECT_FROM_ACTIONS_COUNT);
			while (count > IConstants.MAX_ACTIONS / 2) {
				logger.info("Pruning : " + count + " entities, " + Thread.currentThread().hashCode());
				Map<String, Object> parameters = new HashMap<String, Object>();
				delete(dataBase, ikube.model.Action.class, ikube.model.Action.SELECT_FROM_ACTIONS, parameters);
				count = dataBase.execute(Long.class, ikube.model.Action.SELECT_FROM_ACTIONS_COUNT);
			}
		} finally {
			logger.info("Pruning releasing cluster : " + Thread.currentThread().hashCode());
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getSimpleName(), "", Boolean.FALSE);
			logger.info("Pruning released cluster : " + Thread.currentThread().hashCode());
		}
		return Boolean.TRUE;
	}

	protected void delete(final IDataBase dataBase, final Class<?> klass, final String sql, final Map<String, Object> parameters) {
		try {
			logger.info("Pruning looking for entities to delete : " + Thread.currentThread().hashCode());
			List<?> list = dataBase.find(klass, sql, parameters, 0, IConstants.RESET_DELETE_BATCH_SIZE);
			logger.info("Pruning, deleting entities : " + (list != null ? list.size() : 0) + ", " + Thread.currentThread().hashCode());
			dataBase.removeBatch(list);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}