package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.analytics.AnalyzerManager;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import mockit.Deencapsulation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import weka.clusterers.*;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 14.11.13
 */
public class WekaClustererTest extends AbstractTest {

    private File dataFile;
    private Context context;
    private WekaClusterer wekaclusterer;

    @Before
    public void before() throws Exception {
        dataFile = FileUtilities.findFileRecursively(new File("."), "bank-data.arff");

        context = new Context();
        context.setAlgorithm(EM.class.newInstance());
        context.setName(FileUtilities.cleanFilePath(dataFile.getAbsolutePath()));

        wekaclusterer = new WekaClusterer();
        wekaclusterer.init(context);
        wekaclusterer.build(context);
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
            Analysis<String, double[]> result = wekaclusterer.analyze(analysis);
            for (final double distribution : result.getOutput()) {
                if (Math.abs(distribution) > Math.abs(greatest)) {
                    greatest = distribution;
                }
            }
            // System.out.println("[" + analysis.getClazz() + ", " + greatest + "],");
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
        Instances instances = Deencapsulation.getField(wekaclusterer, "instances");
        double[][] correlationCoEfficients = wekaclusterer.getCorrelationCoefficients(instances);
        for (final double[] correlationCoEfficient : correlationCoEfficients) {
            System.out.println("");
            for (final double instance : correlationCoEfficient) {
                // System.out.print("\t" + instance);
                assertTrue(instance >= -1 && instance <= 1);
            }
        }
    }

    @Test
    public void getDistributionForInstances() throws Exception {
        Instances instances = Deencapsulation.getField(wekaclusterer, "instances");
        double[][] distributionForInstances = wekaclusterer.getDistributionForInstances(instances);
        for (final double[] distribution : distributionForInstances) {
            // logger.info("Dist : " + distribution[0] + ", " + distribution[1]);
            assertTrue(distribution[0] >= 0 && distribution[0] <= 10);
            assertTrue(distribution[1] >= 0 && distribution[1] <= 1);
        }
    }

    @Test
    public void clusterText() throws Exception {
        File dataFile = FileUtilities.findFileRecursively(new File("."), "clustering.arff");

        Context context = new Context();
        context.setAlgorithm(EM.class.newInstance());
        context.setFilter(StringToWordVector.class.newInstance());
        context.setName(FileUtilities.cleanFilePath(dataFile.getAbsolutePath()));

        IAnalyzer<?, ?>[] wekaclusterers = AnalyzerManager.buildAnalyzers(new IAnalyzer.IContext[] { context });

        Analysis<String, double[]> analysis = getAnalysis(null, "Some arbitrary text to cluster into whatever");
        analysis.setCorrelation(Boolean.TRUE);
        analysis.setDistribution(Boolean.TRUE);

        Analysis<String, double[]> result = wekaclusterer.analyze(analysis);
        // logger.info("Result : " + result.getAlgorithmOutput());
        // logger.info("Result : " + result.getClazz());
        for (final double[] correlation : result.getCorrelationCoefficients()) {
            for (final double cor : correlation) {
                logger.info("        : " + cor);
            }
        }
        if (result.getDistributionForInstances() != null) {
            for (final double[] distribution : result.getDistributionForInstances()) {
                for (final double dis : distribution) {
                    logger.info("        : " + dis);
                }
            }
        }
        for (final double output : result.getOutput()) {
            logger.info("        : " + output);
        }
    }

    private void buildAndAnalyze(final String type) throws Exception {
        context.setAlgorithm(Class.forName(type).newInstance());
        wekaclusterer.init(context);
        wekaclusterer.build(context);
        String line = "35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES";
        Analysis<String, double[]> analysis = getAnalysis(null, line);
        Analysis<String, double[]> result = wekaclusterer.analyze(analysis);
        assertNotNull(result.getClazz());
    }

}