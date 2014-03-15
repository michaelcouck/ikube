package ikube.search;

import ikube.analytics.IAnalyticsService;
import ikube.cluster.IClusterManager;
import ikube.model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static ikube.toolkit.ApplicationContextManager.getBean;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Searcher implements Callable<ikube.model.Search> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ikube.model.Search search;

    public Searcher(final ikube.model.Search search) {
        this.search = search;
    }

    @Override
    public ikube.model.Search call() throws Exception {
        try {
            Server local = getClusterManager().getServer();
            Server remote = getClusterManager().getServer();
            String localAddress = local.getAddress();
            String remoteAddress = remote.getAddress();
            logger.info("Executing remote search : " + localAddress + ", " + remoteAddress);
            ikube.model.Search remoteSearch = getSearcherService().doSearch(search);
            logger.info("Finished remote search : " + remoteSearch);
            return remoteSearch;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    IAnalyticsService getAnalyticsService() {
        return getBean(IAnalyticsService.class);
    }

    IClusterManager getClusterManager() {
        return getBean(IClusterManager.class);
    }

    ISearcherService getSearcherService() {
        return getBean(ISearcherService.class);
    }
}
