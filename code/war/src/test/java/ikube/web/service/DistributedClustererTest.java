package ikube.web.service;

import ikube.analytics.weka.WekaClusterer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import weka.clusterers.EM;

/**
 * This test is to see if the clusterers can be distributed in a cluster, and then the results
 * merged based on a probability or a distribution, perhaps a Euclidean distance from the vector
 * to a particular cluster.
 * <p/>
 * NOTE: You can not distribute the clustering over several machines
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-02-2014
 */
@Ignore
public class DistributedClustererTest extends DistributedTest {

    @Before
    public void before() {
        // Take text and build a clusterer
        // Predict each instance and split according to class
        // Build two clusterers with the split data
        // Predict each instance with the new clusterers
        // Compare the prediction using the full data set to the partitioned data sets
    }

    @Test
    @SuppressWarnings("unchecked")
    public void partition() throws Exception {
        String name = "sentiment-tr-clusterer";
        String header = "@relation instances\n@attribute text string\n@data";
        Class<?>[] algorithms = {
                EM.class
                // FarthestFirst.class,
                // CLOPE.class,
                // OPTICS.class,
                // HierarchicalClusterer.class,
                // Cobweb.class,
                // SimpleKMeans.class,
                // sIB.class,
                // XMeans.class
        };
        for (final Class<?> algorithm : algorithms) {
            partitionWith(name, WekaClusterer.class, algorithm, header, 3);
        }
    }

}