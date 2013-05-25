package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.File;
import ikube.model.IndexContext;
import ikube.model.Url;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class resets the data in the cluster. It is imperative that nothing gets reset if there are any servers working of course. The urls
 * that are published into the cluster during the indexing need to be deleted. This deletion action will delete them not only from this
 * server's map but from all the servers' maps.
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
	synchronized boolean internalExecute(final IndexContext<?> indexContext) {
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(IConstants.NAME, indexContext.getName());
			delete(dataBase, Url.class, Url.SELECT_FROM_URL_BY_NAME, parameters);
			delete(dataBase, File.class, File.SELECT_FROM_FILE_BY_NAME, parameters);
			return Boolean.TRUE;
		} finally {
			notifyAll();
		}
	}

	protected synchronized void delete(final IDataBase dataBase, final Class<?> klass, final String sql,
			final Map<String, Object> parameters) {
		do {
			List<?> list = dataBase.find(klass, sql, parameters, 0, IConstants.RESET_DELETE_BATCH_SIZE);
			if (list.size() <= 0) {
				break;
			}
			dataBase.removeBatch(list);
		} while (true);
	}

}