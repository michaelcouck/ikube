package ikube.analytics.neuroph;

import ikube.IConstants;
import ikube.analytics.AAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.*;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.util.io.FileInputAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

import static ikube.Constants.GSON;
import static ikube.toolkit.ThreadUtilities.submit;
import static ikube.toolkit.ThreadUtilities.waitForAnonymousFutures;

/**
 * Document me...
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-08-2014
 */
@SuppressWarnings("UnusedDeclaration")
public class NeurophAnalyzer extends AAnalyzer<Analysis, Analysis, Analysis> {

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

        Iterator optionsIterator = Arrays.asList(options).iterator();
        List<String> stringOptions = new ArrayList<>();
        while (optionsIterator.hasNext()) {
            Object fieldName = optionsIterator.next();
            if (fieldName.toString().startsWith("-") && optionsIterator.hasNext()) {
                stringOptions.add(fieldName.toString());
                stringOptions.add(optionsIterator.next().toString());
            }
        }
        parser.parseArgument(stringOptions);

        Object[] algorithms = context.getAlgorithms();
        Object[] models = context.getModels() != null ? context.getModels() : new Object[algorithms.length];
        File[] dataFiles = getDataFiles(context);
        NeuronProperties neuronProperties = getOption(NeuronProperties.class, options);
        TransferFunctionType transferFunctionType = getOption(TransferFunctionType.class, options);
        for (int i = 0; i < algorithms.length; i++) {
            NeuralNetwork neuralNetwork = createNeuralNetwork(algorithms[i].toString(), options, neuronProperties, transferFunctionType);
            algorithms[i] = neuralNetwork;
            neuralNetwork.setLabel(label);
            LearningRule learningRule = getOption(LearningRule.class, options);
            if (learningRule != null) {
                neuralNetwork.setLearningRule(learningRule);
            }
            // Is this necessary?
            NeuralNetworkType neuralNetworkType = getOption(NeuralNetworkType.class, options);
            if (neuralNetworkType != null) {
                neuralNetwork.setNetworkType(neuralNetworkType);
            }
            // We need these for determining the 'name' of the output?
            if (outputLabels != null) {
                neuralNetwork.setOutputLabels(GSON.fromJson(outputLabels, String[].class));
            }
            // Can we set the weights here? For which networks?
            if (weights != null) {
                neuralNetwork.setWeights(GSON.fromJson(weights, double[].class));
            }
            // The model could have been set in the Spring configuration
            if (models[i] == null) {
                if (outputNeuronsCount <= 0) {
                    models[i] = new DataSet(inputNeuronsCount);
                } else {
                    models[i] = new DataSet(inputNeuronsCount, outputNeuronsCount);
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
    private NeuralNetwork createNeuralNetwork(final String algorithm, final Object[] options, final NeuronProperties neuronProperties,
                                              final TransferFunctionType transferFunctionType) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(algorithm);
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
            Vector<Integer> inputSetsVector = getOption(Vector.class, options, Integer.class);
            if (inputSetsVector == null) {
                double[][] inputPointSets = GSON.fromJson(pointSets, double[][].class);
                double[][] outputPointSets = GSON.fromJson(timeSets, double[][].class);
                neuralNetwork = new NeuroFuzzyPerceptron(inputPointSets, outputPointSets);
            } else {
                neuralNetwork = new NeuroFuzzyPerceptron(inputNeuronsCount, inputSetsVector, outputNeuronsCount);
            }
        } else if (MultiLayerPerceptron.class.isAssignableFrom(clazz)) {
            List<Integer> neuronsInLayer = getOption(List.class, options, Integer.class);
            if (neuronsInLayer != null && !neuronsInLayer.isEmpty()) {
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
        return neuralNetwork;
    }

    private void populateDataSet(final DataSet dataSet, final File dataFile) throws FileNotFoundException {
        FileInputAdapter fileInputAdapter = null;
        try {
            fileInputAdapter = new FileInputAdapter(dataFile) {
                @Override
                public double[] readInput() {
                    try {
                        String inputLine = bufferedReader.readLine();
                        if (inputLine != null) {
                            String[] splitInputLine = StringUtils.split(inputLine, IConstants.DELIMITER_CHARACTERS);
                            double[] inputBuffer = new double[splitInputLine.length];
                            for (int i = 0; i < splitInputLine.length; i++) {
                                inputBuffer[i] = Double.parseDouble(splitInputLine[i]);
                            }
                            return inputBuffer;
                        }
                        return null;
                    } catch (final IOException ex) {
                        throw new RuntimeException("Error reading input from stream : ", ex);
                    }
                }
            };
            double[] data;
            double[] inputData = new double[dataSet.getInputSize()];
            double[] outputData = new double[dataSet.getOutputSize()];
            while ((data = fileInputAdapter.readInput()) != null) {
                System.arraycopy(data, 0, inputData, 0, inputData.length);
                System.arraycopy(data, inputData.length - 1, outputData, 0, outputData.length);
                if (outputData.length == 0) {
                    dataSet.addRow(inputData);
                } else {
                    dataSet.addRow(inputData, outputData);
                }
            }
        } finally {
            if (fileInputAdapter != null) {
                fileInputAdapter.close();
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
        analysis.setInput(stringToDoubleArray(analysis.getInput()));
        analysis.setOutput(stringToDoubleArray(analysis.getOutput()));
    }

    private double[] stringToDoubleArray(final Object object) {
        if (object == null || double[].class.isAssignableFrom(object.getClass())) {
            return (double[]) object;
        }
        if (String.class.isAssignableFrom(object.getClass())) {
            String[] values = StringUtils.split(object.toString().trim(), IConstants.DELIMITER_CHARACTERS);
            double[] doubleArray = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                doubleArray[i] = Double.parseDouble(values[i].trim());
            }
            return doubleArray;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    private <T> T getOption(final Class<T> type, final Object[] options, final Class<?>... types) {
        for (final Object option : options) {
            if (type.isAssignableFrom(option.getClass())) {
                if (Collection.class.isAssignableFrom(option.getClass()) && types != null) {
                    if (!isOneOfType(option, types)) {
                        continue;
                    }
                }
                return (T) option;
            }
        }
        return null;
    }

    private boolean isOneOfType(final Object object, final Class<?>... types) {
        if (object == null || types == null || types.length == 0) {
            return Boolean.FALSE;
        }
        for (final Class<?> type : types) {
            if (type.isAssignableFrom(object.getClass())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

}