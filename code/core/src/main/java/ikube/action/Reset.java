package ikube.action;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.File;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.Url;

import java.util.*;

/**
 * This class resets the data in the cluster. It is imperative that nothing gets reset if there are any servers
 * working of course. The urls that are published into the cluster during the indexing need to be deleted. This
 * deletion action will delete them not only from this server's map but from all the servers' maps.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 31-10-2010
 */
public class Reset extends Action<IndexContext, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized boolean internalExecute(
            final IndexContext indexContext) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(IConstants.NAME, indexContext.getName());
            delete(dataBase, Url.class, Url.SELECT_FROM_URL_BY_NAME, parameters);
            delete(dataBase, File.class, File.SELECT_FROM_FILE_BY_NAME, parameters);
            for (final Indexable child : indexContext.getChildren()) {
                parameters.put(IConstants.NAME, child.getName());
                delete(dataBase, Url.class, Url.SELECT_FROM_URL_BY_NAME, parameters);
                delete(dataBase, File.class, File.SELECT_FROM_FILE_BY_NAME, parameters);
            }
            indexContext.setHashes(new TreeSet<Long>());
            return Boolean.TRUE;
        } finally {
            notifyAll();
        }
    }

    protected synchronized void delete(
            final IDataBase dataBase,
            final Class<?> klass,
            final String sql,
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