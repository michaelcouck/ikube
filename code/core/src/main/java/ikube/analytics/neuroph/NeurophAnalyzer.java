package ikube.analytics.neuroph;

import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-08-2014
 */
public class NeurophAnalyzer implements IAnalyzer<Analysis, Analysis, Analysis> {

    @Override
    public void init(final Context context) throws Exception {
        Object algorithm = new MultiLayerPerceptron(3, 3, 2);
        context.setAlgorithms(algorithm);
    }

    @Override
    public boolean train(final Context context, final Analysis input) throws Exception {
        DataSet dataSet = new DataSet(3, 2);
        double[] inputData = new double[]{0.1, 0.2, 0.3};
        double[] outputData = new double[]{0.3, 0.5};
        dataSet.addRow(inputData, outputData);
        context.setModels(dataSet);
        return true;
    }

    @Override
    public void build(final Context context) throws Exception {
        MultiLayerPerceptron multiLayerPerceptron = (MultiLayerPerceptron) context.getAlgorithms()[0];
        multiLayerPerceptron.getLearningRule().setMaxIterations(1000000);
        multiLayerPerceptron.getLearningRule().setMaxError(0.001);
        DataSet dataSet = (DataSet) context.getModels()[0];
        multiLayerPerceptron.learn(dataSet);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis analyze(final Context context, final Analysis input) throws Exception {
        MultiLayerPerceptron multiLayerPerceptron = (MultiLayerPerceptron) context.getAlgorithms()[0];
        double[] inputData = new double[]{0.1, 0.2, 0.3};
        multiLayerPerceptron.setInput(inputData);
        multiLayerPerceptron.calculate();
        double[] output = multiLayerPerceptron.getOutput();
        input.setOutput(output);
        return input;
    }

    @Override
    public int sizeForClassOrCluster(final Context context, final Analysis clazz) throws Exception {
        return 0;
    }

    @Override
    public void destroy(final Context context) throws Exception {
    }
}
