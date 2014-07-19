package ikube.analytics;

import ikube.IntegrationTest;
import ikube.model.Analysis;
import ikube.model.Context;
import org.junit.After;
import org.junit.Before;
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
    private String analyzerName = "bmw-browsers";
    private String analyzerModelFileName = "bmw-browsers.arff";

    @Autowired
    private IAnalyticsService analyticsService;

    @Before
    public void before() {
        // analyticsService = ApplicationContextManager.getBean(IAnalyticsService.class);
    }

    @After
    public void after() throws Exception {
        destroy();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext(analyzerModelFileName, analyzerName);
        context = analyticsService.create(context);
        assertNotNull(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        create();
        Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
        Context context = analyticsService.train(analysis);
        assertNotNull(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void build() throws Exception {
        train();
        Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
        Context context = analyticsService.build(analysis);
        assertNotNull(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        build();
        Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
        analysis = analyticsService.analyze(analysis);
        assertNotNull(analysis.getOutput());
    }

    @Test
    public void destroy() throws Exception {
        analyze();
        Context context = getContext(analyzerModelFileName, analyzerName);
        analyticsService.destroy(context);
        assertNull(analyticsService.getContext(analyzerName));
    }

}