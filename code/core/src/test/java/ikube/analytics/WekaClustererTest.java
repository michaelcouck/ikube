package ikube.analytics;

import ikube.AbstractTest;
import ikube.model.Buildable;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import weka.clusterers.EM;

/**
 * @author Michael Couck
 * @since 14.11.13
 * @version 01.00
 */
public class WekaClustererTest extends AbstractTest {

	private File dataFile;
	private WekaClusterer wekaClassifier;

	@Before
	public void before() throws Exception {
		dataFile = FileUtilities.findFileRecursively(new File("."), "bank-data.arff");
		Buildable buildable = new Buildable();
		buildable.setFilePath(FileUtilities.cleanFilePath(dataFile.getAbsolutePath()));
		buildable.setType(EM.class.getName());

		wekaClassifier = new WekaClusterer();
		wekaClassifier.build(buildable);
	}

	@Test
	public void analyze() throws Exception {
		List<String> lines = IOUtils.readLines(new FileInputStream(dataFile));
		for (final String line : lines) {
			if (StringUtils.isEmpty(line) || line.startsWith("@")) {
				continue;
			}
			String cluster = wekaClassifier.analyze(line);
			logger.info("Cluster : " + cluster);
		}
	}

}