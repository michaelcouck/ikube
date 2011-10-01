package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.File;
import ikube.model.IndexContext;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class resets the data in the cluster. It is imperative that nothing gets reset if there are any servers working
 * of course. The urls that are published into the cluster during the indexing need to be deleted. This deletion action
 * will delete them not only from this server's map but from all the servers' maps.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Reset extends Action<IndexContext<?>, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		try {
			logger.info("Resetting : " + Thread.currentThread().hashCode());
			IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.NAME, indexContext.getName());
			long count = dataBase.execute(Long.class, Url.SELECT_COUNT_FROM_URL_BY_NAME, parameters);
			if (count > 0) {
				delete(dataBase, Url.class, Url.SELECT_FROM_URL_BY_NAME, parameters);
			}
			count = dataBase.execute(Long.class, File.SELECT_COUNT_FROM_FILE_BY_NAME, parameters);
			if (count > 0) {
				delete(dataBase, File.class, File.SELECT_FROM_FILE_BY_NAME, parameters);
			}
		} finally {
			logger.info("Resetting releasing cluster : " + Thread.currentThread().hashCode());
			logger.error("Action : " + getClusterManager().getServer().getAction());
			getClusterManager().stopWorking(getClass().getSimpleName(), indexContext.getIndexName(), "");
			logger.info("Resetting released cluster : " + Thread.currentThread().hashCode());
			logger.error("Action : " + getClusterManager().getServer().getAction());
			logger.error("Servers : " + getClusterManager().getServers());
		}
		return Boolean.TRUE;
	}

	protected void delete(final IDataBase dataBase, final Class<?> klass, final String sql, final Map<String, Object> parameters) {
		try {
			logger.info("Resetting looking for entities to delete : " + Thread.currentThread().hashCode());
			List<?> list = dataBase.find(klass, sql, parameters, 0, IConstants.RESET_DELETE_BATCH_SIZE);
			do {
				logger.info("Resetting, deleting entities : " + (list != null ? list.size() : 0) + ", " + Thread.currentThread().hashCode());
				dataBase.removeBatch(list);
				list = dataBase.find(klass, sql, parameters, 0, IConstants.RESET_DELETE_BATCH_SIZE);
			} while (list.size() > 0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}