package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.File;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Url;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;

/**
 * This class resets the data in the cluster. It is imperative that nothing gets reset if there are any servers working of course. The urls
 * that are published into the cluster during the indexing need to be deleted. This deletion action will delete them not only from this
 * server's map but from all the servers' maps.
 * 
 * The actions need to be cleaned when all the servers are finished working.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Reset extends Action<IndexContext, Boolean> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext indexContext) {
		try {
			List<Server> servers = getClusterManager().getServers();
			boolean anyWorking = Boolean.FALSE;
			for (Server server : servers) {
				// This is a double check on the working flag
				if (server.getWorking()) {
					anyWorking = Boolean.TRUE;
					continue;
				}
				server.setAction(null);
				getClusterManager().set(Server.class.getName(), server.getId(), server);
			}
			if (!anyWorking) {
				getClusterManager().clear(IConstants.URL);
				getClusterManager().clear(IConstants.URL_DONE);
				getClusterManager().clear(IConstants.URL_ID);
			}
			IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
			delete(dataBase, Url.class, File.class);
		} finally {
			getClusterManager().setWorking(indexContext.getIndexName(), this.getClass().getSimpleName(), "", Boolean.FALSE);
		}
		return Boolean.TRUE;
	}

	protected void delete(final IDataBase dataBase, final Class<?>... klasses) {
		for (Class<?> klass : klasses) {
			try {
				List<?> list = dataBase.find(klass, 0, 1000);
				do {
					dataBase.removeBatch(list);
					list = dataBase.find(klass, 0, 1000);
				} while (list.size() > 0);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
	}

}