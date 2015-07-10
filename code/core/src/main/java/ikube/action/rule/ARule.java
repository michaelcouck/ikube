package ikube.action.rule;

import ikube.action.index.IndexManager;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * This is the base class for rules, just with common functionality that could be used by many rules.
 *
 * @param <T>
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2011
 */
public abstract class ARule<T> implements IRule<T> {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier(value = "ikube.cluster.IClusterManager")
    protected transient IClusterManager clusterManager;

    /**
     * This method goes through all the server index directories in the latest index directory and
     * checks that each index is created and not corrupt. All indexes that are still locked are ignored.
     *
     * @param baseIndexDirectoryPath the path to the base index directory
     * @return whether all the server indexes are created and not corrupt
     */
    protected boolean indexesExist(final String baseIndexDirectoryPath) {
        boolean exists = Boolean.FALSE;
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(baseIndexDirectoryPath);
        if (latestIndexDirectory != null) {
            File[] serverIndexDirectories = latestIndexDirectory.listFiles();
            if (serverIndexDirectories != null && serverIndexDirectories.length > 0) {
                for (File serverIndexDirectory : serverIndexDirectories) {
                    exists = new DirectoryExistsAndNotLocked().evaluate(serverIndexDirectory);
                    if (!exists) {
                        break;
                    }
                }
            }
        }
        return exists;
    }

    /**
     * This method checks to see that the index has not passed it's validity period, i.e. that the age of the index,
     * determined by it's folder name, is not older than the max age that is defined in the index context for the index.
     *
     * @param indexContext       the index context to check for up to date index(es)
     * @param indexDirectoryPath the index directory path to the indexes for this context, could be the back indexes too of course
     * @return whether the index defined by the index path is current
     */
    protected boolean isIndexCurrent(final IndexContext indexContext, final String indexDirectoryPath) {
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
        if (latestIndexDirectory == null) {
            return Boolean.FALSE;
        }
        if (indexContext.isDelta()) {
            return Boolean.TRUE;
        }
        String indexDirectoryName = latestIndexDirectory.getName();
        long currentTime = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
        long indexDirectoryTime = TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(indexDirectoryName));
        long indexAge = currentTime - indexDirectoryTime;
        if (indexAge > indexContext.getMaxAge()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}