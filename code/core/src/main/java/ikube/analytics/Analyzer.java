package ikube.analytics;

import ikube.model.Analysis;
import ikube.model.Buildable;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * TODO Document me...
 * 
 * @author Michael Couck
 * @since 18.11.13
 * @version 01.00
 */
abstract class Analyzer implements IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Analyzer.class);

	/**
	 * This method will create an instance from the input string. The string is assumed to be a comma separated list of values, with the same dimensions as the
	 * attributes in the instances data set. If not, then the results are undefined.
	 * 
	 * @param string the input string, a comma separated list of values, i.e. '35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES'
	 * @return the instance, with the attributes set to the values of the tokens in the input string
	 */
	Instance instance(final String string, final Instances instances) {
		String[] values = StringUtils.split(string, ',');
		Instance instance = new Instance(instances.numAttributes());
		instance.setMissing(0);
		for (int i = instances.numAttributes() - 1, j = values.length - 1; i >= 1 && j >= 0; i--, j--) {
			String value = values[j];
			Attribute attribute = instances.attribute(i);
			if (!attribute.isString()) {
				instance.setValue(attribute, value);
			} else {
				instance.setValue(attribute, attribute.addStringValue(value));
			}
		}
		instance.setDataset(instances);
		return instance;
	}

	/**
	 * This method is for accessing the training/structure file and instantiating an {@link Instances} object.
	 * 
	 * @param buildable the configuration object to build the instances object from
	 * @return the instances object built from the arff training and structure file
	 * @throws IOException
	 */
	Instances instances(final Buildable buildable) throws IOException {
		String filePath = buildable.getTrainingFilePath();
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
			return new Instances(reader);
		} finally {
			if (inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}

}