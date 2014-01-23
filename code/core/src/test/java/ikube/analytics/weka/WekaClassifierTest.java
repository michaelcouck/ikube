package ikube.analytics.weka;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.IAnalyzer;
import ikube.model.Analysis;
import ikube.model.Context;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.StringToWordVector;


/**
 * @author Michael Couck
 * @version 01.00
 * @since 08.09.13
 */
public class WekaClassifierTest extends AbstractTest {

    /**
     * Class under test
     */
    private IAnalyzer<Analysis<String, double[]>, Analysis<String, double[]>> wekaClassifier;

    private Context<WekaClassifier, StringToWordVector, SMO> context;
    private String positive = "my beautiful little girl";

    @Before
    public void before() throws Exception {
        context = new Context<>();
        context.setAlgorithm(SMO.class.newInstance());
        context.setFilter(StringToWordVector.class.newInstance());
        context.setName(IConstants.CLASSIFICATION);

        wekaClassifier = new WekaClassifier();
    }

    @Test
    public void init() throws Exception {
        IAnalyzer.IContext context = mock(IAnalyzer.IContext.class);
        when(context.getName()).thenReturn("sentiment");
        wekaClassifier.init(context);
        verify(context, atLeastOnce()).getAlgorithm();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        wekaClassifier.init(context);
        int iterations = context.getMaxTraining() + 1;
        do {
            Analysis<String, double[]> analysis = getAnalysis(IConstants.POSITIVE, positive);
            wekaClassifier.train(analysis);
        } while (iterations-- >= 0);
    }

    @Test
    public void build() throws Exception {
        wekaClassifier.init(context);
        wekaClassifier.build(context);
    }

    @Test
    public void analyze() throws Exception {
        wekaClassifier.init(context);
        wekaClassifier.build(context);

        Analysis<String, double[]> analysis = getAnalysis(null, positive);
        Analysis<String, double[]> result = wekaClassifier.analyze(analysis);
        assertEquals(IConstants.POSITIVE, result.getClazz());

        String negative = "you selfish stupid woman";
        analysis = getAnalysis(null, negative);
        result = wekaClassifier.analyze(analysis);
        assertEquals(IConstants.NEGATIVE, result.getClazz());
    }

}