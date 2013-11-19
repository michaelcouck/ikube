package ikube.analytics;

import ikube.model.Buildable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import weka.clusterers.Clusterer;
import weka.core.Instances;

/**
 * @author Michael Couck
 * @since 10.04.13
 * @version 01.00
 */
public class WekaClusterer implements IAnalyzer<String, String> {

	private Clusterer clusterer;

	@Override
	public void build(final Buildable buildable) throws Exception {
		String filePath = buildable.getFilePath();
		File file = new File(filePath);
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream);
		Instances instances = new Instances(reader);

		clusterer = (Clusterer) Class.forName(buildable.getType()).newInstance();
		clusterer.buildClusterer(instances);
	}

	@Override
	public boolean train(final String... strings) throws Exception {
		return true;
	}

	@Override
	public String analyze(final String input) throws Exception {
		return null;
	}

}