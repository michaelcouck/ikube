package ikube.analytics;

import ikube.IntegrationTest;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        context = analyticsService.create(context);
        assertNotNull(context);
        context = analyticsService.getContext(contextName);
        assertNotNull(context);
        for (int i = 0; i < context.getAlgorithms().length; i++) {
            assertNotNull(context.getAlgorithms()[i]);
            assertNotNull(context.getModels()[i]);
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

}