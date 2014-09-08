package ikube.analytics.neuroph;

import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.*;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Future;

import static ikube.Constants.GSON;
import static ikube.toolkit.ThreadUtilities.waitForAnonymousFutures;

/**
 * Document me...
 *
 * @author Michael Couck
 * @version 01.00
 * @see org.neuroph.nnet.Adaline#Adaline(int)
 * @see org.neuroph.nnet.AutoencoderNetwork#AutoencoderNetwork(int...)
 * @see org.neuroph.nnet.BAM#BAM(int inputNeuronsCount, int outputNeuronsCount)
 * @see org.neuroph.nnet.CompetitiveNetwork#CompetitiveNetwork(int inputNeuronsCount, int outputNeuronsCount)
 * @see org.neuroph.nnet.ConvolutionalNetwork#ConvolutionalNetwork()
 * @see org.neuroph.nnet.ElmanNetwork#ElmanNetwork(int inputNeuronsCount, int hiddenNeuronsCount, int contextNeuronsCount, int outputNeuronsCount)
 * @see org.neuroph.nnet.Hopfield#Hopfield(int neuronsCount)
 * @see org.neuroph.nnet.Hopfield#Hopfield(int neuronsCount, NeuronProperties neuronProperties)
 * @see org.neuroph.nnet.Instar#Instar(int inputNeuronsCount)
 * @see org.neuroph.nnet.UnsupervisedHebbianNetwork#UnsupervisedHebbianNetwork(int inputNeuronsNum, int outputNeuronsNum)
 * @see org.neuroph.nnet.UnsupervisedHebbianNetwork#UnsupervisedHebbianNetwork(int inputNeuronsNum, int outputNeuronsNum, TransferFunctionType transferFunctionType)
 * @see org.neuroph.nnet.SupervisedHebbianNetwork#SupervisedHebbianNetwork(int inputNeuronsNum, int outputNeuronsNum)
 * @see org.neuroph.nnet.SupervisedHebbianNetwork#SupervisedHebbianNetwork(int inputNeuronsNum, int outputNeuronsNum, TransferFunctionType transferFunctionType)
 * @see org.neuroph.nnet.RBFNetwork#RBFNetwork(int inputNeuronsCount, int rbfNeuronsCount, int outputNeuronsCount)
 * @see org.neuroph.nnet.Perceptron#Perceptron(int inputNeuronsCount, int outputNeuronsCount)
 * @see org.neuroph.nnet.Perceptron#Perceptron(int inputNeuronsCount, int outputNeuronsCount, TransferFunctionType transferFunctionType)
 * @see org.neuroph.nnet.Outstar#Outstar(int outputNeuronsCount)
 * @see org.neuroph.nnet.NeuroFuzzyPerceptron#NeuroFuzzyPerceptron(double[][] pointsSets, double[][] timeSets)
 * @see org.neuroph.nnet.NeuroFuzzyPerceptron#NeuroFuzzyPerceptron(int inputNum, Vector inputSets, int outNum)
 * @see org.neuroph.nnet.MultiLayerPerceptron#MultiLayerPerceptron(List neuronsInLayers)
 * @see org.neuroph.nnet.MultiLayerPerceptron#MultiLayerPerceptron(int... neuronsInLayers)
 * @see org.neuroph.nnet.MultiLayerPerceptron#MultiLayerPerceptron(TransferFunctionType transferFunctionType, int...)
 * @see org.neuroph.nnet.MultiLayerPerceptron#MultiLayerPerceptron(List neuronsInLayers, TransferFunctionType transferFunctionType)
 * @see org.neuroph.nnet.MultiLayerPerceptron#MultiLayerPerceptron(List neuronsInLayers, NeuronProperties neuronProperties)
 * @see org.neuroph.nnet.MaxNet#MaxNet(int neuronsCount)
 * @see org.neuroph.nnet.Kohonen#Kohonen(int inputNeuronsCount, int outputNeuronsCount)
 * @see org.neuroph.nnet.JordanNetwork#JordanNetwork(int inputNeuronsCount, int hiddenNeuronsCount, int contextNeuronsCount, int outputNeuronsCount)
 * @since 29-08-2014
 */
@SuppressWarnings("UnusedDeclaration")
public class NeurophAnalyzer implements IAnalyzer<Analysis, Analysis, Analysis> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeurophAnalyzer.class);

    private Random random;

    /**
     * Below are the parameters for initialising the neural networks.
     */

    @Option(name = "-label", usage = "This is a simple string.")
    private String label;
    @Option(name = "-weights", usage = "This is a string that converts to a double[] in Json, i.e. [1.0, 0.0, ...]")
    private String weights;
    @Option(name = "-outputLabels", usage = "This is a string that converts to a String[] in Json, i.e. [one, two, ...]")
    private String outputLabels;
    @Option(name = "-inputNeuronsCount", usage = "Simple int input parameter")
    private int inputNeuronsCount;
    @Option(name = "-hiddenNeuronsCount", usage = "Simple int input parameter")
    private int hiddenNeuronsCount;
    @Option(name = "-contextNeuronsCount", usage = "Simple int input parameter")
    private int contextNeuronsCount;
    @Option(name = "-rbfNeuronsCount", usage = "Simple int input parameter")
    private int rbfNeuronsCount;
    @Option(name = "-outputNeuronsCount", usage = "Simple int input parameter")
    private int outputNeuronsCount;
    @Option(name = "-pointSets", usage = "This is a string that converts to a double[][] in Json, i.e. [[1.0, 1.0, ...], [1.0, 1.0, ...], ...]")
    private String pointSets;
    @Option(name = "-timeSets", usage = "This is a string that converts to a double[][] in Json, i.e. [[1.0, 1.0, ...], [1.0, 1.0, ...], ...]")
    private String timeSets;
    @Option(name = "-neuronsInLayers", usage = "This is a string that converts to an int[] in Json, i.e. [1, 2, ...]")
    private String neuronsInLayers;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void init(final Context context) throws Exception {
        random = new Random();
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(1024);

        Object[] options = context.getOptions();

        for (final Object option : options) {
            if (option.getClass().isArray()) {
                String[] stringOptions = null;
                if (String[].class.isAssignableFrom(option.getClass())) {
                    stringOptions = (String[]) option;
                } else if (Object[].class.isAssignableFrom(option.getClass())) {
                    Object[] objects = (Object[]) option;
                    stringOptions = new String[objects.length];
                    for (int i = 0; i < objects.length; i++) {
                        stringOptions[i] = objects[i].toString();
                    }
                }
                parser.parseArgument(stringOptions);
            }
        }

        Object[] algorithms = context.getAlgorithms();
        Object[] models = new Object[algorithms.length];
        NeuronProperties neuronProperties = getOption(NeuronProperties.class, options);
        TransferFunctionType transferFunctionType = getOption(TransferFunctionType.class, options);
        for (int i = 0; i < algorithms.length; i++) {
            Class<?> clazz = Class.forName(algorithms[i].toString());
            NeuralNetwork neuralNetwork = null;
            if (Adaline.class.isAssignableFrom(clazz)) {
                neuralNetwork = new Adaline(inputNeuronsCount);
            } else if (AutoencoderNetwork.class.isAssignableFrom(clazz)) {
                int[] neuronsInLayersArray = GSON.fromJson(neuronsInLayers, int[].class);
                neuralNetwork = new AutoencoderNetwork(neuronsInLayersArray);
            } else if (BAM.class.isAssignableFrom(clazz)) {
                neuralNetwork = new BAM(inputNeuronsCount, outputNeuronsCount);
            } else if (CompetitiveNetwork.class.isAssignableFrom(clazz)) {
                neuralNetwork = new CompetitiveNetwork(inputNeuronsCount, outputNeuronsCount);
            } else if (ConvolutionalNetwork.class.isAssignableFrom(clazz)) {
                neuralNetwork = new ConvolutionalNetwork();
            } else if (ElmanNetwork.class.isAssignableFrom(clazz)) {
                neuralNetwork = new ElmanNetwork(inputNeuronsCount, hiddenNeuronsCount, contextNeuronsCount, outputNeuronsCount);
            } else if (Hopfield.class.isAssignableFrom(clazz)) {
                if (neuronProperties == null) {
                    neuralNetwork = new Hopfield(inputNeuronsCount);
                } else {
                    neuralNetwork = new Hopfield(inputNeuronsCount, neuronProperties);
                }
            } else if (Instar.class.isAssignableFrom(clazz)) {
                neuralNetwork = new Instar(inputNeuronsCount);
            } else if (UnsupervisedHebbianNetwork.class.isAssignableFrom(clazz)) {
                if (transferFunctionType == null) {
                    neuralNetwork = new UnsupervisedHebbianNetwork(inputNeuronsCount, outputNeuronsCount);
                } else {
                    neuralNetwork = new UnsupervisedHebbianNetwork(inputNeuronsCount, outputNeuronsCount, transferFunctionType);
                }
            } else if (SupervisedHebbianNetwork.class.isAssignableFrom(clazz)) {
                if (transferFunctionType == null) {
                    neuralNetwork = new SupervisedHebbianNetwork(inputNeuronsCount, outputNeuronsCount);
                } else {
                    neuralNetwork = new SupervisedHebbianNetwork(inputNeuronsCount, outputNeuronsCount, transferFunctionType);
                }
            } else if (RBFNetwork.class.isAssignableFrom(clazz)) {
                neuralNetwork = new RBFNetwork(inputNeuronsCount, rbfNeuronsCount, outputNeuronsCount);
            } else if (Perceptron.class.isAssignableFrom(clazz)) {
                if (transferFunctionType == null) {
                    neuralNetwork = new Perceptron(inputNeuronsCount, outputNeuronsCount);
                } else {
                    neuralNetwork = new Perceptron(inputNeuronsCount, outputNeuronsCount, transferFunctionType);
                }
            } else if (Outstar.class.isAssignableFrom(clazz)) {
                neuralNetwork = new Outstar(outputNeuronsCount);
            } else if (NeuroFuzzyPerceptron.class.isAssignableFrom(clazz)) {
                Vector<Integer> inputSetsVector = getOption(Vector.class, options);
                if (inputSetsVector == null) {
                    double[][] inputPointSets = GSON.fromJson(pointSets, double[][].class);
                    double[][] outputPointSets = GSON.fromJson(timeSets, double[][].class);
                    neuralNetwork = new NeuroFuzzyPerceptron(inputPointSets, outputPointSets);
                } else {
                    neuralNetwork = new NeuroFuzzyPerceptron(inputNeuronsCount, inputSetsVector, outputNeuronsCount);
                }
            } else if (MultiLayerPerceptron.class.isAssignableFrom(clazz)) {
                List<Integer> neuronsInLayer = getOption(List.class, options);
                if (neuronsInLayer != null) {
                    if (transferFunctionType != null) {
                        neuralNetwork = new MultiLayerPerceptron(neuronsInLayer, transferFunctionType);
                    } else if (neuronProperties != null) {
                        neuralNetwork = new MultiLayerPerceptron(neuronsInLayer, neuronProperties);
                    } else {
                        neuralNetwork = new MultiLayerPerceptron(neuronsInLayer);
                    }
                } else {
                    if (neuronsInLayers != null) {
                        int[] neuronsInLayersArray = GSON.fromJson(neuronsInLayers, int[].class);
                        if (transferFunctionType != null) {
                            neuralNetwork = new MultiLayerPerceptron(transferFunctionType, neuronsInLayersArray);
                        } else {
                            neuralNetwork = new MultiLayerPerceptron(neuronsInLayersArray);
                        }
                    }
                }
            } else if (MaxNet.class.isAssignableFrom(clazz)) {
                neuralNetwork = new MaxNet(inputNeuronsCount);
            } else if (Kohonen.class.isAssignableFrom(clazz)) {
                neuralNetwork = new Kohonen(inputNeuronsCount, outputNeuronsCount);
            } else if (JordanNetwork.class.isAssignableFrom(clazz)) {
                neuralNetwork = new JordanNetwork(inputNeuronsCount, hiddenNeuronsCount, contextNeuronsCount, outputNeuronsCount);
            }
            algorithms[i] = neuralNetwork;
            neuralNetwork.setLabel(label);
            LearningRule learningRule = getOption(LearningRule.class, options);
            if (learningRule != null) {
                neuralNetwork.setLearningRule(learningRule);
            }
            NeuralNetworkType neuralNetworkType = getOption(NeuralNetworkType.class, options);
            if (neuralNetworkType != null) {
                neuralNetwork.setNetworkType(neuralNetworkType);
            }
            if (outputLabels != null) {
                neuralNetwork.setOutputLabels(GSON.fromJson(outputLabels, String[].class));
            }
            if (weights != null) {
                neuralNetwork.setWeights(GSON.fromJson(weights, double[].class));
            }
            if (outputNeuronsCount > 0) {
                models[i] = new DataSet(inputNeuronsCount, outputNeuronsCount);
            } else {
                models[i] = new DataSet(inputNeuronsCount);
            }
        }
        context.setModels(models);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean train(final Context context, final Analysis input) throws Exception {
        Object[] models = context.getModels();
        int index = random.nextInt(Math.max(1, models.length - 1));
        DataSet dataSet = (DataSet) models[Math.min(index, models.length - 1)];
        double[] inputData = (double[]) input.getInput();
        double[] outputData = (double[]) input.getOutput();
        dataSet.addRow(inputData, outputData);
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
            Future future = ThreadUtilities.submit(this.getClass().getName(), new Runnable() {
                public void run() {
                    NeuralNetwork neuralNetwork = (NeuralNetwork) algorithms[index];
                    LOGGER.warn("Building neural network : " + neuralNetwork.getClass().getName());
                    neuralNetwork.learn((DataSet) models[index]);
                }
            });
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
    public Analysis analyze(final Context context, final Analysis input) throws Exception {
        // This is a naive approach, purely an aggregation,
        // perhaps this should be reviewed by Zoran?
        Object[] algorithms = context.getAlgorithms();
        double[] inputData = (double[]) input.getInput();
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
        input.setOutput(aggregateOutput);
        return input;
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

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    private <T> T getOption(final Class<T> type, final Object[] options) {
        for (final Object option : options) {
            if (type.isAssignableFrom(option.getClass())) {
                return (T) option;
            }
        }
        return null;
    }

}
