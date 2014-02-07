package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import mockit.Deencapsulation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import weka.clusterers.*;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 14.11.13
 */
public class WekaClustererTest extends AbstractTest {

    private File dataFile;
    private Context context;
    private WekaClusterer wekaClusterer;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        dataFile = FileUtilities.findFileRecursively(new File("."), "bmw-browsers.arff");

        context = new Context();
        context.setAlgorithm(EM.class.newInstance());
        context.setName(FilenameUtils.getBaseName(dataFile.getName()));
        context.setMaxTraining(Integer.MAX_VALUE);
        context.setOptions(new String[]{"-N", "6"});

        wekaClusterer = new WekaClusterer();
        wekaClusterer.init(context);
        wekaClusterer.build(context);
    }

    @Test
    public void analyze() throws Exception {
        List<String> lines = IOUtils.readLines(new FileInputStream(dataFile));
        for (final String line : lines) {
            if (StringUtils.isEmpty(line) || line.startsWith("@")) {
                continue;
            }
            double greatest = 0;
            Analysis<String, double[]> analysis = getAnalysis(null, line);
            Analysis<String, double[]> result = wekaClusterer.analyze(analysis);
            Integer index = 0;
            double[] output = result.getOutput();
            for (int i = 0; i < output.length; i++) {
                double distribution = output[i];
                if (Math.abs(distribution) > Math.abs(greatest)) {
                    greatest = distribution;
                    index = i;
                }
            }
            assertEquals(index.toString(), result.getClazz());
        }
    }

    @Test
    public void buildAndAnalyzeAll() throws Exception {
        // These are not interesting clusterers
        // buildAndAnalyze(DBSCAN.class.getName());
        // buildAndAnalyze(OPTICS.class.getName());

        buildAndAnalyze(CLOPE.class.getName());
        buildAndAnalyze(HierarchicalClusterer.class.getName());
        buildAndAnalyze(Cobweb.class.getName());
        buildAndAnalyze(FarthestFirst.class.getName());
        buildAndAnalyze(SimpleKMeans.class.getName());

        // These are specialized and need to be integrated properly
        // buildAndAnalyze(sIB.class.getName());
        // buildAndAnalyze(XMeans.class.getName());
    }

    @Test
    public void getCorrelationCoEfficients() throws Exception {
        Instances instances = Deencapsulation.getField(wekaClusterer, "instances");
        double[] correlationCoEfficients = wekaClusterer.getCorrelationCoefficients(instances);
        for (final double correlationCoEfficient : correlationCoEfficients) {
            logger.info("Correlation : " + correlationCoEfficient);
            assertTrue(correlationCoEfficient >= -1 && correlationCoEfficient <= 1);
        }
    }

    @Test
    public void getDistributionForInstances() throws Exception {
        Instances instances = Deencapsulation.getField(wekaClusterer, "instances");
        double[][] distributionForInstances = wekaClusterer.getDistributionForInstances(instances);
        for (final double[] distribution : distributionForInstances) {
            logger.info("Probability : " + Arrays.toString(distribution));
            for (final double probability : distribution) {
                // logger.info("Probability : " + probability);
                assertTrue(probability >= 0 && probability <= 1.0);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cluster() throws Exception {
        Instances instances = Deencapsulation.getField(wekaClusterer, "instances");

        String line = "0,1,0,0,1,1,1,1";
        Analysis<String, double[]> analysis = getAnalysis(null, line);
        Analysis<String, double[]> result = wekaClusterer.analyze(analysis);
        assertEquals("This instance is in the second cluster : ", "3", result.getClazz());

        Enumeration<Instance> instanceEnumeration = instances.enumerateInstances();
        while (instanceEnumeration.hasMoreElements()) {
            Instance instance = instanceEnumeration.nextElement();
            double classOrCluster = wekaClusterer.classOrCluster(instance);
            // The clusters are one to three
            assertTrue(classOrCluster >= 0.0 && classOrCluster <= 6);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void classesOrClusters() throws Exception {
        Object[] clusters = wekaClusterer.classesOrClusters();
        int[] clusterSizes = new int[]{12, 29, 19, 19, 10, 11};
        assertEquals(6, clusters.length);
        assertEquals(0.0, clusters[0]);
        assertEquals(1.0, clusters[1]);
        assertEquals(2.0, clusters[2]);
        assertEquals(3.0, clusters[3]);
        assertEquals(4.0, clusters[4]);
        assertEquals(5.0, clusters[5]);

        for (int i = 0; i < clusters.length; i++) {
            final Object cluster = clusters[i];
            Analysis analysis = getAnalysis(cluster.toString(), null);
            int size = wekaClusterer.sizeForClassOrCluster(analysis);
            assertEquals(clusterSizes[i], size);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private void printInstances(final Instances instances) {
        logger.info("Num attributes : " + instances.numAttributes());
        logger.info("Num instances : " + instances.numInstances());
        logger.info("Sum of weights : " + instances.sumOfWeights());
        for (int i = 0; i < instances.numAttributes(); i++) {
            Attribute attribute = instances.attribute(i);
            logger.info("Attribute : " + attribute);
        }
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            logger.info("Instance : " + instance);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildAndAnalyze(final String type) throws Exception {
        context.setAlgorithm(Class.forName(type).newInstance());
        wekaClusterer.init(context);
        wekaClusterer.build(context);
        String line = "0,0,0,1,1,0,0,0";
        Analysis<String, double[]> analysis = getAnalysis(null, line);
        Analysis<String, double[]> result = wekaClusterer.analyze(analysis);
        assertNotNull(result.getClazz());
    }

}