package ikube.analytics.neuroph;

import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.OsUtilities;
import ikube.toolkit.StringUtilities;
import mockit.Deencapsulation;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.*;
import org.neuroph.util.NeuralNetworkType;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;

import java.util.*;

import static junit.framework.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-08-2014
 */
public class NeurophAnalyzerTest extends AbstractTest {

    @Spy
    private Context context;
    @Spy
    private Analysis analysis;
    @Mock
    private LearningRule learningRule;

    @Spy
    @InjectMocks
    private NeurophAnalyzer neurophAnalyzer;

    private Object[] algorithms;
    private Object[] options;
    private NeuronProperties neuronProperties;
    private Vector<Integer> inputSetsVector;
    private List<Integer> neuronsInLayer;

    @Before
    public void before() {
        neuronProperties = new NeuronProperties();
        inputSetsVector = new Vector<>(Arrays.asList(1, 2, 3));
        neuronsInLayer = new ArrayList<>(Arrays.asList(1, 2, 3));
    }

    @Test
    public void init() throws Exception {
        algorithms = new Object[]{Adaline.class.getName(), AutoencoderNetwork.class.getName(), BAM.class.getName(),
                CompetitiveNetwork.class.getName(), ConvolutionalNetwork.class.getName(), // ElmanNetwork.class.getName(),
                Hopfield.class.getName(), Instar.class.getName(), UnsupervisedHebbianNetwork.class.getName(), SupervisedHebbianNetwork.class.getName(),
                RBFNetwork.class.getName(), Perceptron.class.getName(), Outstar.class.getName(), // NeuroFuzzyPerceptron.class.getName(),
                MultiLayerPerceptron.class.getName(), MaxNet.class.getName(), Kohonen.class.getName(), JordanNetwork.class.getName()};

        options = new Object[]{
                "-label", "label",
                "-outputLabels", "[one, two, three]",
                "-inputNeuronsCount", "3",
                "-hiddenNeuronsCount", "3",
                "-contextNeuronsCount", "3",
                "-rbfNeuronsCount", "3",
                "-outputNeuronsCount", "3",
                "-pointSets", "[[1, 1, 1, 1], [0, 0, 0, 0], [1, 1, 1, 0]]",
                "-timeSets", "[[1, 1, 1, 1], [0, 0, 0, 0], [1, 1, 1, 0]]",
                "-neuronsInLayers", "[3, 3, 3]",
                learningRule, NeuralNetworkType.ADALINE, neuronProperties, TransferFunctionType.GAUSSIAN,
                inputSetsVector, neuronsInLayer};
        if (!OsUtilities.isOs("3.11.0-12-generic")) {
            options = setFieldValues(options);
        }

        when(context.getAlgorithms()).thenReturn(algorithms);
        when(context.getOptions()).thenReturn(options);

        neurophAnalyzer.init(context);
        for (final Object algorithm : context.getAlgorithms()) {
            assertTrue(NeuralNetwork.class.isAssignableFrom(algorithm.getClass()));
        }
        for (final Object model : context.getModels()) {
            assertNotNull(model);
        }
    }

    private Object[] setFieldValues(final Object[] options) {
        List<Object> result = new ArrayList<>();
        Iterator optionsIterator = Arrays.asList(options).iterator();
        while (optionsIterator.hasNext()) {
            Object option = optionsIterator.next();
            if (option.toString().startsWith("-") && optionsIterator.hasNext()) {
                Object fieldValue = optionsIterator.next();
                logger.warn("Setting field : " + option + ":" + fieldValue);
                if (StringUtilities.isNumeric(fieldValue.toString())) {
                    fieldValue = Integer.parseInt(fieldValue.toString());
                }
                Deencapsulation.setField(neurophAnalyzer, StringUtils.strip(option.toString(), "-"), fieldValue);
            } else {
                result.add(option);
            }
        }
        return result.toArray();
    }

    @Test
    public void train() throws Exception {
        init();
        when(analysis.getInput()).thenReturn(new double[]{1, 0, 1});
        when(analysis.getOutput()).thenReturn(new double[]{1, 0, 1});
        neurophAnalyzer.train(context, analysis);
        Object[] models = context.getModels();
        boolean populated = Boolean.FALSE;
        for (final Object model : models) {
            DataSet dataSet = (DataSet) model;
            List<DataSetRow> dataSetRows = dataSet.getRows();
            for (final DataSetRow dataSetRow : dataSetRows) {
                boolean inputOutputEqual = Arrays.equals(dataSetRow.getInput(), (double[]) analysis.getInput())
                        & Arrays.equals(dataSetRow.getDesiredOutput(), (double[]) analysis.getOutput());
                if (populated && inputOutputEqual) {
                    break;
                }
                if (inputOutputEqual) {
                    populated = Boolean.TRUE;
                }
            }
        }
        if (!populated) {
            fail("Model not populated in the training method : ");
        }
    }

    @Test
    public void build() throws Exception {
        train();
        neurophAnalyzer.build(context);
        Object[] algorithms = context.getAlgorithms();
        for (final Object algorithm : algorithms) {
            NeuralNetwork neuralNetwork = (NeuralNetwork) algorithm;
            Double[] weights = neuralNetwork.getWeights();
            logger.warn(neuralNetwork.getClass().getName() + ":" + Arrays.deepToString(weights));
        }
        assertTrue(context.isBuilt());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        algorithms = new Object[]{Hopfield.class.getName(), Kohonen.class.getName(), RBFNetwork.class.getName()};
        options = new Object[]{
                "-label", "label",
                "-outputLabels", "[one, two, three, four]",
                "-inputNeuronsCount", "4",
                "-hiddenNeuronsCount", "4",
                "-contextNeuronsCount", "4",
                "-rbfNeuronsCount", "4",
                "-outputNeuronsCount", "4",
                "-neuronsInLayers", "[8, 8, 4]",
                learningRule};

        if (!OsUtilities.isOs("3.11.0-12-generic")) {
            options = setFieldValues(options);
        }
        String[] fileNames = {"bmw-browsers.csv"};

        when(context.getAlgorithms()).thenReturn(algorithms);
        when(context.getOptions()).thenReturn(options);
        when(context.getFileNames()).thenReturn(fileNames);

        when(analysis.getInput()).thenReturn(new double[]{0, 1, 1, 0});
        when(analysis.getOutput()).thenReturn(new double[]{1, 0, 1, 1});

        neurophAnalyzer.init(context);
        for (int i = 0; i < 1000; i++) {
            neurophAnalyzer.train(context, analysis);
        }
        neurophAnalyzer.build(context);

        neurophAnalyzer.analyze(context, analysis);
        Mockito.verify(analysis, Mockito.atLeast(1000)).setOutput(any());
    }

}