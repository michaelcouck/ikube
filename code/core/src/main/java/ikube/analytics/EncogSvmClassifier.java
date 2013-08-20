package ikube.analytics;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.svm.SVM;
import org.encog.ml.svm.training.SVMSearchTrain;
import org.encog.ml.svm.training.SVMTrain;
import org.encog.ml.train.MLTrain;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.som.SOM;

public class EncogSvmClassifier implements IClassifier<String, String, Object, Object> {

	@SuppressWarnings("unused")
	public void initialize() {
		SVM svm = new SVM();
		NeuralDataSet dataSet = null;
		MLDataSet mlDataSet = new BasicMLDataSet();
		MLData mlData = new BasicMLData(new double[] {});
		mlDataSet.add(mlData);
		SVMTrain svmTrain = new SVMTrain(svm, mlDataSet);
		MLTrain mlTrain = new SVMSearchTrain(svm, mlDataSet);
		SOM som = new SOM();
	}

	@Override
	public String classify(final String input) {
		return null;
	}

	@Override
	public Object train(Object trainingInput) {
		return null;
	}

}
