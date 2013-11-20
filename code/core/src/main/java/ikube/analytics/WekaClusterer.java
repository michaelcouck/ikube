package ikube.analytics;

import ikube.model.Buildable;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class WekaClusterer implements IAnalyzer<String, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClusterer.class);

	private Clusterer clusterer;
	private Instances instances;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final Buildable buildable) throws Exception {
		String filePath = buildable.getFilePath();
		InputStream inputStream = null;
		try {
			File file = new File(filePath);
			if (file == null || !file.exists() || !file.canRead()) {
				LOGGER.warn("Can't find data file : " + filePath + ", will search for it...");
				String fileName = FilenameUtils.getName(filePath);
				file = FileUtilities.findFileRecursively(new File("."), fileName);
				if (file == null || !file.exists() || !file.canRead()) {
					throw new RuntimeException("Couldn't find file for analyzer or can't read file : " + filePath);
				} else {
					LOGGER.info("Found data file : " + file.getAbsolutePath());
				}
			}
			inputStream = new FileInputStream(file);
			Reader reader = new InputStreamReader(inputStream);
			instances = new Instances(reader);
		} finally {
			if (inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
		}
		String type = buildable.getType();
		clusterer = (Clusterer) Class.forName(type).newInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean train(final String... strings) throws Exception {
		for (final String string : strings) {
			Instance instance = instance(string);
			instances.add(instance);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build(final Buildable buildable) throws Exception {
		clusterer.buildClusterer(instances);
		if (buildable.isLog()) {
			log();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String analyze(final String input) throws Exception {
		Instance instance = instance(input);
		int cluster = clusterer.clusterInstance(instance);
		return Integer.toString(cluster);
	}

	/**
	 * This method will create an instance from the input string. The string is assumed to be a comma separated list of values, with the same dimensions as the
	 * attributes in the instances data set. If not, then the results are undefined.
	 * 
	 * @param string the input string, a comma separated list of values, i.e. '35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES'
	 * @return the instance, with the attributes set to the values of the tokens in the input string
	 */
	private Instance instance(final String string) {
		String[] values = StringUtils.split(string, ',');
		Instance instance = new Instance(instances.numAttributes());
		for (int i = 0; i < values.length; i++) {
			instance.setValue(instances.attribute(i), values[i]);
		}
		instance.setDataset(instances);
		return instance;
	}

	private void log() throws Exception {
		ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
		clusterEvaluation.setClusterer(clusterer);
		clusterEvaluation.evaluateClusterer(instances);
		LOGGER.info("Num clusters : " + clusterEvaluation.clusterResultsToString());
		for (int i = 0; i < instances.numAttributes(); i++) {
			Attribute attribute = instances.attribute(i);
			LOGGER.info("Attribute : " + attribute.name() + ", " + attribute.type());
			for (int j = 0; j < attribute.numValues(); j++) {
				LOGGER.info("          : " + attribute.value(j));
			}
		}
	}

}