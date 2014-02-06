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
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileInputStream;
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
        dataFile = FileUtilities.findFileRecursively(new File("."), "bank-data.arff");

        context = new Context();
        context.setAlgorithm(EM.class.newInstance());
        context.setName(FilenameUtils.getBaseName(dataFile.getName()));
        context.setMaxTraining(Integer.MAX_VALUE);

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
            logger.info("Result : " + result.getClazz() + ", " + result);
            for (final double distribution : result.getOutput()) {
                logger.info("Distribution : " + distribution);
                if (Math.abs(distribution) > Math.abs(greatest)) {
                    greatest = distribution;
                }
            }
            assertNotNull(result.getClazz());
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
            for (final double probability : distribution) {
                logger.info("Probability : " + probability);
                assertTrue(probability >= 0 && probability <= 1.0);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void cluster() throws Exception {
        dataFile = FileUtilities.findFileRecursively(new File("."), "bmw-browsers.arff");

        context = new Context();
        context.setAlgorithm(EM.class.newInstance());
        context.setName(FilenameUtils.getBaseName(dataFile.getName()));
        context.setMaxTraining(Integer.MAX_VALUE);

        wekaClusterer = new WekaClusterer();
        wekaClusterer.init(context);
        wekaClusterer.build(context);
        Instances instances = Deencapsulation.getField(wekaClusterer, "instances");

        String line = "0,1,0,0,1,1,1,1";
        Analysis<String, double[]> analysis = getAnalysis(null, line);
        Analysis<String, double[]> result = wekaClusterer.analyze(analysis);
        assertEquals("This instance is in the second cluster : ", "2", result.getClazz());

        Enumeration<Instance> instanceEnumeration = instances.enumerateInstances();
        while (instanceEnumeration.hasMoreElements()) {
            Instance instance = instanceEnumeration.nextElement();
            double classOrCluster = wekaClusterer.classOrCluster(instance);
            // The clusters are one to three
            assertTrue(classOrCluster >= 0.0 && classOrCluster <= 2);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildAndAnalyze(final String type) throws Exception {
        context.setAlgorithm(Class.forName(type).newInstance());
        wekaClusterer.init(context);
        wekaClusterer.build(context);
        String line = "35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES";
        Analysis<String, double[]> analysis = getAnalysis(null, line);
        Analysis<String, double[]> result = wekaClusterer.analyze(analysis);
        assertNotNull(result.getClazz());
    }

}