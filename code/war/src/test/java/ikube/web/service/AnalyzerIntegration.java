package ikube.web.service;

import ikube.AbstractTest;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.THREAD;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Arrays;

import static ikube.toolkit.REST.doGet;
import static ikube.toolkit.REST.doPost;
import static junit.framework.Assert.*;

/**
 * Note to self: For some reason this test does not work on the Dell server!!!
 * Update: Does now it seems... :)
 * This test must still be completed and verified, perhaps with all sorts of analytics, like regression etc.
 * Update: Done.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-02-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class AnalyzerIntegration extends AbstractTest {

    private int sleep = 3000;
    private String line = "1,1,0,1,1,0,1,1";
    private String contextName = "bmw-browsers";
    private String dataFileName = "bmw-browsers.arff";

    @Before
    public void before() {
        try {
            destroy();
        } catch (final Exception e) {
            logger.error(null, e);
        }
    }

    @After
    public void after() {
        try {
            destroy();
        } catch (final Exception e) {
            logger.error(null, e);
        }
    }

    @Test
    @Ignore
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext(dataFileName, contextName);
        String url = getAnalyzerUrl(Analyzer.CREATE);
        Context result = doPost(url, context, Context.class);
        THREAD.sleep(sleep);
        assertNotNull(result);
        assertTrue(result.getAlgorithms().length > 0);
        assertNotNull(result.getAnalyzer());
        for (int i = 0; i < result.getAlgorithms().length; i++) {
            assertNotNull(result.getAlgorithms()[i]);
        }
    }

    @Test
    @Ignore
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        create();

        Analysis<String, double[]> analysis = getAnalysis(contextName, line);
        String url = getAnalyzerUrl(Analyzer.TRAIN);
        Analysis result = doPost(url, analysis, Analysis.class);
        THREAD.sleep(sleep);
        assertNotNull(result);
    }

    @Test
    @Ignore
    public void build() throws Exception {
        train();

        Analysis analysis = getAnalysis(contextName, null);
        String url = getAnalyzerUrl(Analyzer.BUILD);
        Context context = doPost(url, analysis, Context.class);
        THREAD.sleep(sleep);
        assertTrue(context.isBuilt());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        build();

        Analysis<String, double[]> analysis = getAnalysis(contextName, line);
        String url = getAnalyzerUrl(Analyzer.ANALYZE);
        Analysis result = doPost(url, analysis, Analysis.class);
        THREAD.sleep(sleep);
        assertTrue(Integer.parseInt(result.getClazz()) >= 0 && Integer.parseInt(result.getClazz()) <= 6);
    }

    @Test
    @Ignore
    public void destroy() throws Exception {
        analyze();

        Analysis analysis = getAnalysis(contextName, null);
        String url = getAnalyzerUrl(Analyzer.CONTEXT);
        Context context = doPost(url, analysis, Context.class);
        THREAD.sleep(sleep);

        assertNotNull(context);

        String destroyUrl = getAnalyzerUrl(Analyzer.DESTROY);
        doPost(destroyUrl, context, Context.class);
        THREAD.sleep(sleep);

        context = doPost(url, analysis, Context.class);
        assertNull(context);
    }

    @Test
    @Ignore
    public void context() throws Exception {
        build();

        Analysis analysis = getAnalysis(contextName, null);
        String url = getAnalyzerUrl(Analyzer.CONTEXT);
        Context context = doPost(url, analysis, Context.class);
        THREAD.sleep(sleep);
        assertNotNull(context);
    }

    @Test
    @Ignore
    @SuppressWarnings("unchecked")
    public void contexts() throws Exception {
        build();

        String contextsUrl = getAnalyzerUrl(Analyzer.CONTEXTS);
        String[] contexts = doGet(contextsUrl, String[].class);
        THREAD.sleep(sleep);
        assertTrue(Arrays.toString(contexts).contains(this.contextName));
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getAnalyzerUrl(final String service) throws MalformedURLException {
        return getUrl(Analyzer.ANALYZER + service);
    }

}