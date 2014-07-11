package ikube.analytics;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.tools.weka.WekaClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;

public class JavaMlClassifier implements IClassifier<String, String, Object, Object> {

	public JavaMlClassifier() {
		Dataset dataset = new DefaultDataset();
		Classifier wekaClassifier = new WekaClassifier(new Logistic());
		wekaClassifier.buildClassifier(dataset);
		wekaClassifier = new WekaClassifier(new SMO());
		wekaClassifier.buildClassifier(dataset);
		wekaClassifier = new WekaClassifier(new SimpleLogistic());
		wekaClassifier.buildClassifier(dataset);
		wekaClassifier = new WekaClassifier(new NaiveBayes());
		wekaClassifier.buildClassifier(dataset);
	}

	@Override
	public String classify(String input) {
		return null;
	}

	@Override
	public Object train(Object trainingInput) {
		return null;
	}

}