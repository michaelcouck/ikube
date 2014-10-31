package ikube.analytics.neuroph;

import ikube.analytics.AAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.*;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Future;

import static ikube.toolkit.CsvUtilities.getCsvData;
import static ikube.toolkit.FileUtilities.cleanFilePath;
import static ikube.toolkit.MatrixUtilities.objectVectorToDoubleVector;
import static ikube.toolkit.MatrixUtilities.stringVectorDoubleVector;
import static ikube.toolkit.ThreadUtilities.submit;
import static ikube.toolkit.ThreadUtilities.waitForAnonymousFutures;

/**
 * This adapter wraps the Neuroph neural networks. As with all the {@link ikube.analytics.IAnalyzer} implementations,
 * there are multiple networks defined for each analyzer, potentially all different. The result of the committee of networks
 * is an aggregation of each one.
 * <p/>
 * The analysis is not parallelized, but the building of the networks is. This facilitates larger training sets, and can
 * build the models in a reasonable time, as opposed to a single threaded model building operation.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-08-2014
 */
@SuppressWarnings("UnusedDeclaration")
public class NeurophAnalyzer extends AAnalyzer<Analysis, Analysis, Analysis> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeurophAnalyzer.class);

    private Random random;
    private NeurophAnalyzerOption options;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void init(final Context context) throws Exception {
        random = new Random();
        options = new NeurophAnalyzerOption(context.getOptions());

        Object[] algorithms = context.getAlgorithms();
        Object[] models = context.getModels() != null ? context.getModels() : new Object[algorithms.length];
        File[] dataFiles = getDataFiles(context);
        NeuronProperties neuronProperties = options.getOption(NeuronProperties.class);
        TransferFunctionType transferFunctionType = options.getOption(TransferFunctionType.class);
        for (int i = 0; i < algorithms.length; i++) {
            NeuralNetwork neuralNetwork = createNeuralNetwork(algorithms[i].toString(), neuronProperties, transferFunctionType);
            algorithms[i] = neuralNetwork;
            neuralNetwork.setLabel(options.getLabel());
            LearningRule learningRule = options.getOption(LearningRule.class);
            if (learningRule != null) {
                neuralNetwork.setLearningRule(learningRule);
            }
            // Is this necessary?
            NeuralNetworkType neuralNetworkType = options.getOption(NeuralNetworkType.class);
            if (neuralNetworkType != null) {
                neuralNetwork.setNetworkType(neuralNetworkType);
            }
            // We need these for determining the 'name' of the output?
            if (options.getOutputLabels() != null) {
                neuralNetwork.setOutputLabels(options.getOutputLabelsArray());
            }
            // Can we set the weights here? For which networks?
            if (options.getWeights() != null) {
                neuralNetwork.setWeights(options.getWeightsArray());
            }
            // The model could have been set in the Spring configuration
            if (models[i] == null) {
                if (options.getOutputNeuronsCount() <= 0) {
                    models[i] = new DataSet(options.getInputNeuronsCount());
                } else {
                    models[i] = new DataSet(options.getInputNeuronsCount(), options.getOutputNeuronsCount());
                }
            }
            if (dataFiles != null && dataFiles.length > i) {
                populateDataSet((DataSet) models[i], dataFiles[i]);
            }
            // TODO: Populate/train the models with the training datas if there are any
        }
        // Set the options to null because this causes havoc with the
        // learning rule and Gson, they don't play nicely together at all
        context.setOptions();
        context.setModels(models);
    }

    @SuppressWarnings("unchecked")
    private NeuralNetwork createNeuralNetwork(final String algorithm, final NeuronProperties neuronProperties,
                                              final TransferFunctionType transferFunctionType) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(algorithm);
        NeuralNetwork neuralNetwork = null;
        if (Adaline.class.isAssignableFrom(clazz)) {
            neuralNetwork = new Adaline(options.getInputNeuronsCount());
        } else if (AutoencoderNetwork.class.isAssignableFrom(clazz)) {
            neuralNetwork = new AutoencoderNetwork(options.getNeuronsInLayersArray());
        } else if (BAM.class.isAssignableFrom(clazz)) {
            neuralNetwork = new BAM(options.getInputNeuronsCount(), options.getOutputNeuronsCount());
        } else if (CompetitiveNetwork.class.isAssignableFrom(clazz)) {
            neuralNetwork = new CompetitiveNetwork(options.getInputNeuronsCount(), options.getOutputNeuronsCount());
        } else if (ConvolutionalNetwork.class.isAssignableFrom(clazz)) {
            neuralNetwork = new ConvolutionalNetwork();
        } else if (ElmanNetwork.class.isAssignableFrom(clazz)) {
            neuralNetwork = new ElmanNetwork(options.getInputNeuronsCount(),
                    options.getHiddenNeuronsCount(), options.getContextNeuronsCount(), options.getOutputNeuronsCount());
        } else if (Hopfield.class.isAssignableFrom(clazz)) {
            if (neuronProperties == null) {
                neuralNetwork = new Hopfield(options.getInputNeuronsCount());
            } else {
                neuralNetwork = new Hopfield(options.getInputNeuronsCount(), neuronProperties);
            }
        } else if (Instar.class.isAssignableFrom(clazz)) {
            neuralNetwork = new Instar(options.getInputNeuronsCount());
        } else if (UnsupervisedHebbianNetwork.class.isAssignableFrom(clazz)) {
            if (transferFunctionType == null) {
                neuralNetwork = new UnsupervisedHebbianNetwork(options.getInputNeuronsCount(), options.getOutputNeuronsCount());
            } else {
                neuralNetwork = new UnsupervisedHebbianNetwork(options.getInputNeuronsCount(), options.getOutputNeuronsCount(),
                        transferFunctionType);
            }
        } else if (SupervisedHebbianNetwork.class.isAssignableFrom(clazz)) {
            if (transferFunctionType == null) {
                neuralNetwork = new SupervisedHebbianNetwork(options.getInputNeuronsCount(), options.getOutputNeuronsCount());
            } else {
                neuralNetwork = new SupervisedHebbianNetwork(options.getInputNeuronsCount(), options.getOutputNeuronsCount(),
                        transferFunctionType);
            }
        } else if (RBFNetwork.class.isAssignableFrom(clazz)) {
            neuralNetwork = new RBFNetwork(options.getInputNeuronsCount(), options.getRbfNeuronsCount(),
                    options.getOutputNeuronsCount());
        } else if (Perceptron.class.isAssignableFrom(clazz)) {
            if (transferFunctionType == null) {
                neuralNetwork = new Perceptron(options.getInputNeuronsCount(), options.getOutputNeuronsCount());
            } else {
                neuralNetwork = new Perceptron(options.getInputNeuronsCount(), options.getOutputNeuronsCount(), transferFunctionType);
            }
        } else if (Outstar.class.isAssignableFrom(clazz)) {
            neuralNetwork = new Outstar(options.getOutputNeuronsCount());
        } else if (NeuroFuzzyPerceptron.class.isAssignableFrom(clazz)) {
            Vector<Integer> inputSetsVector = options.getInputSetsVector();
            if (inputSetsVector == null) {
                neuralNetwork = new NeuroFuzzyPerceptron(options.getPointSetsMatrix(), options.getTimeSetsMatrix());
            } else {
                neuralNetwork = new NeuroFuzzyPerceptron(options.getInputNeuronsCount(), inputSetsVector,
                        options.getOutputNeuronsCount());
            }
        } else if (MultiLayerPerceptron.class.isAssignableFrom(clazz)) {
            List<Integer> neuronsInLayer = options.getNeuronsInLayer();
            if (neuronsInLayer != null && !neuronsInLayer.isEmpty()) {
                if (transferFunctionType != null) {
                    neuralNetwork = new MultiLayerPerceptron(neuronsInLayer, transferFunctionType);
                } else if (neuronProperties != null) {
                    neuralNetwork = new MultiLayerPerceptron(neuronsInLayer, neuronProperties);
                } else {
                    neuralNetwork = new MultiLayerPerceptron(neuronsInLayer);
                }
            } else {
                if (options.getNeuronsInLayers() != null) {
                    int[] neuronsInLayersArray = options.getNeuronsInLayersArray();
                    if (transferFunctionType != null) {
                        neuralNetwork = new MultiLayerPerceptron(transferFunctionType, neuronsInLayersArray);
                    } else {
                        neuralNetwork = new MultiLayerPerceptron(neuronsInLayersArray);
                    }
                }
            }
        } else if (MaxNet.class.isAssignableFrom(clazz)) {
            neuralNetwork = new MaxNet(options.getInputNeuronsCount());
        } else if (Kohonen.class.isAssignableFrom(clazz)) {
            neuralNetwork = new Kohonen(options.getInputNeuronsCount(), options.getOutputNeuronsCount());
        } else if (JordanNetwork.class.isAssignableFrom(clazz)) {
            neuralNetwork = new JordanNetwork(options.getInputNeuronsCount(), options.getHiddenNeuronsCount(),
                    options.getContextNeuronsCount(), options.getOutputNeuronsCount());
        }
        if (neuralNetwork == null) {
            throw new RuntimeException("Network null, have you specified all the parameters to create it?");
        }
        return neuralNetwork;
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    private void populateDataSet(final DataSet dataSet, final File dataFile) throws FileNotFoundException {
        Object[][] matrix = getCsvData(cleanFilePath(dataFile.getAbsolutePath()));

        double[] data;
        double[] inputData = new double[dataSet.getInputSize()];
        double[] outputData = new double[dataSet.getOutputSize()];
        for (final Object[] row : matrix) {
            double[] doubleVector = objectVectorToDoubleVector(row);
            System.arraycopy(doubleVector, 0, inputData, 0, inputData.length);
            System.arraycopy(doubleVector, inputData.length - 1, outputData, 0, outputData.length);
            if (outputData.length == 0) {
                dataSet.addRow(inputData);
            } else {
                dataSet.addRow(inputData, outputData);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean train(final Context context, final Analysis analysis) throws Exception {
        inputOutputStringToDoubleArray(analysis);
        Object[] models = context.getModels();
        int index = random.nextInt(Math.max(1, models.length - 1));
        DataSet dataSet = (DataSet) models[Math.min(index, models.length - 1)];
        double[] inputData = (double[]) analysis.getInput();
        double[] outputData = (double[]) analysis.getOutput();
        if (outputData == null) {
            // Unsupervised
            dataSet.addRow(inputData);
        } else {
            // Supervised
            dataSet.addRow(inputData, outputData);
        }
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(final Context context) throws Exception {
        final Object[] algorithms = context.getAlgorithms();
        final Object[] models = context.getModels();
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < algorithms.length; i++) {
            final int index = i;
            class AnalyzerBuilder implements Runnable {
                public void run() {
                    NeuralNetwork neuralNetwork = (NeuralNetwork) algorithms[index];
                    DataSet dataSet = (DataSet) models[index];
                    if (dataSet.isEmpty()) {
                        LOGGER.warn("Data set empty for network : " + neuralNetwork.getClass().getName());
                        return;
                    }

                    LOGGER.warn("Building neural network : " + neuralNetwork.getClass().getName());
                    neuralNetwork.learn(dataSet);
                    LOGGER.warn("Finished building neural network : " + neuralNetwork.getClass().getName());
                    LOGGER.warn(ToStringBuilder.reflectionToString(neuralNetwork.getLearningRule()));
                }
            }
            Future future = submit(this.getClass().getName(), new AnalyzerBuilder());
            futures.add(future);
        }
        waitForAnonymousFutures(futures, Integer.MAX_VALUE);
        // TODO: Set the evaluation and the capabilities
        // TODO: Potentially serialize the networks
        context.setBuilt(Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Analysis analyze(final Context context, final Analysis analysis) throws Exception {
        if (!context.isBuilt()) {
            return analysis;
        }

        inputOutputStringToDoubleArray(analysis);
        // This is a naive approach, purely an aggregation,
        // perhaps this should be reviewed by Zoran?
        Object[] algorithms = context.getAlgorithms();
        double[] inputData = (double[]) analysis.getInput();
        double[] aggregateOutput = new double[0];
        for (final Object algorithm : algorithms) {
            NeuralNetwork neuralNetwork = (NeuralNetwork) algorithm;
            LOGGER.debug("Executing : " + neuralNetwork.getClass().getName());
            neuralNetwork.setInput(inputData);
            neuralNetwork.calculate();
            double[] output = neuralNetwork.getOutput();
            if (aggregateOutput.length == 0) {
                aggregateOutput = output;
            } else {
                for (int i = 0; i < output.length; i++) {
                    aggregateOutput[i] += output[i];
                }
            }
        }
        double denominator = context.getAlgorithms().length;
        for (int i = 0; i < aggregateOutput.length; i++) {
            aggregateOutput[i] = aggregateOutput[i] / denominator;
        }
        // TODO: Check that this is the best voting method
        analysis.setOutput(aggregateOutput);
        return analysis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int sizeForClassOrCluster(final Context context, final Analysis analysis) throws Exception {
        // TODO: Get the output from the anns
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy(final Context context) throws Exception {
        // TODO: Destroy here
    }

    @SuppressWarnings("unchecked")
    private void inputOutputStringToDoubleArray(final Analysis analysis) {
        analysis.setInput(stringVectorDoubleVector(analysis.getInput()));
        analysis.setOutput(stringVectorDoubleVector(analysis.getOutput()));
    }

}