package ikube.search;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static ikube.toolkit.ApplicationContextManager.getBean;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Searcher implements Callable<ikube.model.Search>, Serializable {

    private ikube.model.Search search;

    public Searcher(final ikube.model.Search search) {
        this.search = search;
    }

    @Override
    public ikube.model.Search call() throws Exception {
        try {
            // IClusterManager clusterManager = getBean(IClusterManager.class);
            // Server server = clusterManager.getServer();
            // String localAddress = server.getAddress();
            // System.out.println("Executing remote search : " + localAddress);
            ISearcherService searcherService = getBean(ISearcherService.class);
            return searcherService.doSearch(search);
            // System.out.println("Finished remote search : " + remoteSearch);
            // return remoteSearch;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
