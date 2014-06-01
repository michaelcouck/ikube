package ikube.cluster.hzc;

import ikube.IntegrationTest;
import ikube.database.IDataBase;
import ikube.model.Search;
import ikube.toolkit.ApplicationContextManager;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-06-2014
 */
public class ClusterManagerCacheSearchIntegration extends IntegrationTest {

    private ClusterManagerCacheSearch clusterManagerCacheSearch;

    @Before
    public void before() {
        clusterManagerCacheSearch = ApplicationContextManager.getBean(ClusterManagerCacheSearch.class);
    }

    @Test
    public void store() {
        Search search = new Search();
        search.setHash(System.currentTimeMillis());
        clusterManagerCacheSearch.store(search.getHash(), search);

        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            clusterManagerCacheSearch.store(search.getHash(), search);
        }

        IDataBase dataBase = ApplicationContextManager.getBean(IDataBase.class);
        long count = dataBase.count(Search.class);
        Search dbSearch = dataBase.find(Search.class, search.getId());
        Assert.assertEquals(1, count);
        Assert.assertEquals(iterations + 1, dbSearch.getCount());
    }

}
