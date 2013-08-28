package ikube.analytics;

import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.svm.KernelType;
import org.encog.ml.svm.SVM;
import org.encog.ml.svm.SVMType;
import org.encog.ml.svm.training.SVMTrain;
import org.junit.Test;

public class EncogSvmClassifier implements IClassifier<String, String, Object, Object> {

	public void initialize() {
		MLDataSet training = new BasicMLDataSet(XOR.XOR_INPUT, XOR.XOR_IDEAL);
		SVM svm = new SVM(2, SVMType.EpsilonSupportVectorRegression, KernelType.RadialBasisFunction);
		SVMTrain train = new SVMTrain(svm, training);
		train.iteration();

		int result;

		result = svm.classify(new BasicMLData(XOR.XOR_IDEAL[0]));
		System.out.println("Result : " + result);

		result = svm.classify(new BasicMLData(XOR.XOR_IDEAL[1]));
		System.out.println("Result : " + result);

		result = svm.classify(new BasicMLData(XOR.XOR_IDEAL[2]));
		System.out.println("Result : " + result);

		result = svm.classify(new BasicMLData(XOR.XOR_IDEAL[3]));
		System.out.println("Result : " + result);

		result = svm.classify(new BasicMLData(XOR.XOR_INPUT[0]));
		System.out.println("Result : " + result);

		result = svm.classify(new BasicMLData(XOR.XOR_INPUT[1]));
		System.out.println("Result : " + result);

		result = svm.classify(new BasicMLData(XOR.XOR_INPUT[2]));
		System.out.println("Result : " + result);

		result = svm.classify(new BasicMLData(XOR.XOR_INPUT[3]));
		System.out.println("Result : " + result);
	}

	@Override
	public String classify(final String input) {
		return null;
	}

	@Override
	public Object train(Object trainingInput) {
		return null;
	}

	@Test
	public void classify() {
		EncogSvmClassifier encogSvmClassifier = new EncogSvmClassifier();
		encogSvmClassifier.initialize();
	}

}
