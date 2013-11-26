package ikube.analytics;

import static junit.framework.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Buildable;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

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

/**
 * @author Michael Couck
 * @since 14.11.13
 * @version 01.00
 */
public class WekaClustererTest extends AbstractTest {

	private File dataFile;
	private Buildable buildable;
	private WekaClusterer wekaClassifier;
	private String line = "35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES";

	@Before
	public void before() throws Exception {
		dataFile = FileUtilities.findFileRecursively(new File("."), "bank-data.arff");

		buildable = new Buildable();
		buildable.setTrainingFilePath(FileUtilities.cleanFilePath(dataFile.getAbsolutePath()));

		wekaClassifier = new WekaClusterer();
	}

	@Test
	public void analyze() throws Exception {
		buildable.setAlgorithmType(EM.class.getName());
		wekaClassifier.init(buildable);
		wekaClassifier.build(buildable);
		List<String> lines = IOUtils.readLines(new FileInputStream(dataFile));
		for (final String line : lines) {
			if (StringUtils.isEmpty(line) || line.startsWith("@")) {
				continue;
			}
		}
		Analysis<String, double[]> analysis = getAnalysis(null, line);
		Analysis<String, double[]> result = wekaClassifier.analyze(analysis);
		assertNotNull(result.getClazz());
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

	private void buildAndAnalyze(final String type) throws Exception {
		buildable.setAlgorithmType(type);
		wekaClassifier.init(buildable);
		wekaClassifier.build(buildable);
		Analysis<String, double[]> analysis = getAnalysis(null, line);
		Analysis<String, double[]> result = wekaClassifier.analyze(analysis);
		assertNotNull(result.getClazz());
	}

}