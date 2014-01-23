package ikube.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaClusterer;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import org.junit.Before;
import org.junit.Test;
import weka.clusterers.EM;

import java.io.File;
import java.util.HashMap;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20.11.13
 */
public class AnalyticsServiceTest extends AbstractTest {

    private Context context;
    private IAnalyzer<?, ?> analyzer;
    private AnalyticsService analyticsService;
    private Analysis<String, double[]> analysis;

    @Before
    public void before() throws Exception {
        String line = "35_51,FEMALE,INNER_CITY,0_24386,NO,1,NO,NO,NO,NO,YES";

        analysis = new Analysis<>();
        analysis.setAnalyzer("analyzer-em");
        analysis.setInput(line);

        context = new Context();
        context.setName("bank-data");
        context.setAlgorithm(EM.class.newInstance());
        analyzer = new WekaClusterer();
        analyzer.init(context);
        analyzer.build(context);
        analyticsService = new AnalyticsService();
        analyticsService.setAnalyzers(new HashMap<String, IAnalyzer<?, ?>>() {
            {
                put(analysis.getAnalyzer(), analyzer);
            }
        });

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String string = gson.toJson(analysis);
        logger.info(string);
    }

    @Test
    public void analyze() {
        analyticsService.analyze(analysis);
        Integer clazz = Integer.parseInt(analysis.getClazz());
        assertTrue(clazz == 0 || clazz == 1);
    }

    @Test
    public void getAnalyzer() throws Exception {
        File externalConfig = FileUtilities.findDirectoryRecursively(new File("."), "external");
        File springConfig = FileUtilities.findFileRecursively(externalConfig, "spring\\.xml");
        String springConfigPath = FileUtilities.cleanFilePath(springConfig.getAbsolutePath());
        System.setProperty(IConstants.IKUBE_CONFIGURATION, springConfigPath);

        analysis.setAnalyzer("analyzer-em-different");
        // analysis.setBuildable(context);

        context.setAlgorithm(EM.class.newInstance());
        context.setAnalyzer(WekaClusterer.class.newInstance());

        IAnalyzer<?, ?> analyzer = analyticsService.getAnalyzer(analysis);
        assertNotNull(analyzer);
    }

}