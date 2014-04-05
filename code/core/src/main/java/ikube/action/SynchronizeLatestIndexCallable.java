package ikube.action;

import ikube.action.index.IndexManager;
import ikube.model.IndexContext;
import ikube.toolkit.SerializationUtilities;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * This callable will get the latest index directory on the target server.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-04-2014
 */
public class SynchronizeLatestIndexCallable implements Callable<String[]>, Serializable {

    private Date latestTimestamp;
    private IndexContext indexContext;

    public SynchronizeLatestIndexCallable(final IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    @Override
    public String[] call() throws Exception {
        latestTimestamp = IndexManager.getLatestIndexDirectoryDate(indexContext);
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
        return latestIndexDirectory.list();
    }

    public Date getLatestTimestamp() {
        return (Date) SerializationUtilities.clone(latestTimestamp);
    }

}
