package ikube.action;

import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Url;

import java.util.Iterator;
import java.util.List;

/**
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Reset extends Action<IndexContext, Boolean> {

	@Override
	public Boolean execute(IndexContext indexContext) {
		try {
			boolean anyWorking = getClusterManager().anyWorkingOnIndex(indexContext);
			if (!anyWorking) {
				getClusterManager().setWorking(indexContext, getClass().getName(), Boolean.TRUE, System.currentTimeMillis());
				logger.debug("Resetting : " + !anyWorking + ", " + indexContext);

				int batch = 1000;
				IDataBase dataBase = getDataBase();
				List<Url> urls = dataBase.find(Url.class, 0, batch);
				Iterator<Url> iterator = urls.iterator();
				while (iterator.hasNext()) {
					Url url = iterator.next();
					logger.debug("Deleting url : " + url);
					dataBase.remove(url);
					if (!iterator.hasNext()) {
						urls = dataBase.find(Url.class, 0, batch);
						iterator = urls.iterator();
					}
				}
				indexContext.setIdNumber(0);
				indexContext.getCache().clear();
				return Boolean.TRUE;
			} else {
				logger.debug("Not resetting : " + !anyWorking + ", " + indexContext);
			}
			return Boolean.FALSE;
		} finally {
			getClusterManager().setWorking(indexContext, null, Boolean.FALSE, 0);
		}
	}

}