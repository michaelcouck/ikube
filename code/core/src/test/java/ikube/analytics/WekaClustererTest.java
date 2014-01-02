package ikube.analytics;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Buildable;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import mockit.Deencapsulation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import weka.clusterers.CLOPE;
import weka.clusterers.Cobweb;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * @author Michael Couck
 * @since 14.11.13
 * @version 01.00
 */
public class WekaClustererTest extends AbstractTest {

	private File dataFile;
	private Buildable buildable;
	private WekaClusterer wekaclusterer;
	private String line = "35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES";

	@Before
	public void before() throws Exception {
		dataFile = FileUtilities.findFileRecursively(new File("."), "bank-data.arff");

		buildable = new Buildable();
		buildable.setAlgorithmType(EM.class.getName());
		buildable.setTrainingFilePath(FileUtilities.cleanFilePath(dataFile.getAbsolutePath()));

		wekaclusterer = new WekaClusterer();
		wekaclusterer.init(buildable);
		wekaclusterer.build(buildable);
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

		Buildable buildable = new Buildable();
		buildable.setAlgorithmType(EM.class.getName());
		buildable.setFilterType(StringToWordVector.class.getName());
		buildable.setTrainingFilePath(FileUtilities.cleanFilePath(dataFile.getAbsolutePath()));

		WekaClusterer wekaclusterer = (WekaClusterer) AnalyzerManager.buildAnalyzer(buildable, new WekaClusterer());

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
		buildable.setAlgorithmType(type);
		wekaclusterer.init(buildable);
		wekaclusterer.build(buildable);
		Analysis<String, double[]> analysis = getAnalysis(null, line);
		Analysis<String, double[]> result = wekaclusterer.analyze(analysis);
		assertNotNull(result.getClazz());
	}

}