package ikube.analytics.neuroph;

import org.kohsuke.args4j.Option;

import java.util.List;
import java.util.Vector;

import static ikube.Constants.GSON;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-09-2014
 */
@SuppressWarnings("UnusedDeclaration")
public class NeurophAnalyzerOption extends ikube.analytics.Option {

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

    NeurophAnalyzerOption(final Object[] options) {
        super(options);
    }

    String getLabel() {
        return label;
    }

    String getWeights() {
        return weights;
    }

    String getOutputLabels() {
        return outputLabels;
    }

    int getInputNeuronsCount() {
        return inputNeuronsCount;
    }

    int getHiddenNeuronsCount() {
        return hiddenNeuronsCount;
    }

    int getContextNeuronsCount() {
        return contextNeuronsCount;
    }

    int getRbfNeuronsCount() {
        return rbfNeuronsCount;
    }

    int getOutputNeuronsCount() {
        return outputNeuronsCount;
    }

    String getPointSets() {
        return pointSets;
    }

    String getTimeSets() {
        return timeSets;
    }

    String getNeuronsInLayers() {
        return neuronsInLayers;
    }

    double[][] getPointSetsMatrix() {
        return GSON.fromJson(getPointSets(), double[][].class);
    }

    double[][] getTimeSetsMatrix() {
        return GSON.fromJson(getTimeSets(), double[][].class);
    }

    Vector<Integer> getInputSetsVector() {
        return getOption(Vector.class, Integer.class);
    }

    int[] getNeuronsInLayersArray() {
        return GSON.fromJson(getNeuronsInLayers(), int[].class);
    }

    String[] getOutputLabelsArray() {
        return GSON.fromJson(getOutputLabels(), String[].class);
    }

    double[] getWeightsArray() {
        return GSON.fromJson(getWeights(), double[].class);
    }

    List<Integer> getNeuronsInLayer() {
        return getOption(List.class, Integer.class);
    }

}