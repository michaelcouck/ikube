package ikube.analytics;

import ikube.IntegrationTest;
import ikube.analytics.weka.WekaClusterer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FILE;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import weka.clusterers.SimpleKMeans;

import java.io.File;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-04-2014
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class AnalyticsServiceIntegration extends IntegrationTest {

    private String line = "1,1,0,1,1,0,1,1";
    private String contextName = "bmw-browsers";
    private String analyzerModelFileName = "bmw-browsers.arff";

    @Autowired
    private IAnalyticsService analyticsService;

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext(analyzerModelFileName, contextName);
        Context createdContext = analyticsService.create(context);
        assertNotNull(createdContext);
        createdContext = analyticsService.getContext(contextName);
        assertNotNull("The context must be created : ", createdContext);
        for (int i = 0; i < createdContext.getAlgorithms().length; i++) {
            assertNotNull(createdContext.getAlgorithms()[i]);
            assertNotNull(createdContext.getModels()[i]);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        create();
        Analysis<String, double[]> analysis = getAnalysis(contextName, line);
        Context context = analyticsService.train(analysis);
        assertNotNull(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void build() throws Exception {
        train();
        Analysis<String, double[]> analysis = getAnalysis(contextName, line);
        Context context = analyticsService.build(analysis);
        assertNotNull(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        build();
        Analysis<String, double[]> analysis = getAnalysis(contextName, line);
        analysis = analyticsService.analyze(analysis);
        assertNotNull(analysis.getOutput());
    }

    @Test
    public void destroy() throws Exception {
        analyze();
        Context context = getContext(analyzerModelFileName, contextName);
        analyticsService.destroy(context);
        context = analyticsService.getContext(contextName);
        assertNull(context);
    }

    protected Context getContext(final String fileName, final String name) throws Exception {
        File trainingDataFile = FILE.findFileRecursively(new File("."), fileName);
        String trainingData = FILE.getContent(trainingDataFile);

        Context context = new Context();
        context.setName(name);
        context.setAnalyzer(WekaClusterer.class.getName());
        context.setAlgorithms(SimpleKMeans.class.getName());
        context.setOptions("-N", "6");

        context.setTrainingDatas(trainingData);
        context.setMaxTrainings(Integer.MAX_VALUE);

        return context;
    }

    protected Analysis getAnalysis(final String context, final String input) {
        Analysis<String, double[]> analysis = new Analysis<>();
        analysis.setContext(context);
        analysis.setInput(input);
        return analysis;
    }

}