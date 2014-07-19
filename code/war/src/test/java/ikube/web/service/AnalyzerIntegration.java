package ikube.web.service;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.HttpClientUtilities;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * TODO: Note to self. For some reason this test does not work on the Dell server!!!
 * TODO: Does now it seems... :)
 * TODO: This test must still be completed and verified, perhaps with all sorts of analytics, like regression etc.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-02-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class AnalyzerIntegration extends IntegrationTest {

    private String line = "1,1,0,1,1,0,1,1";
    private String analyzerName = "bmw-browsers";
    private String analyzerModelFileName = "bmw-browsers.arff";

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext(analyzerModelFileName, analyzerName);
        String url = getUrl(Analyzer.CREATE);
        HttpClientUtilities.doPost(url, context, Context.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        create();

        Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
        String url = getUrl(Analyzer.TRAIN);
        HttpClientUtilities.doPost(url, analysis, Analysis.class);
    }

    @Test
    public void build() throws Exception {
        train();

        Analysis analysis = getAnalysis(analyzerName, null);
        String url = getUrl(Analyzer.BUILD);
        HttpClientUtilities.doPost(url, analysis, Analysis.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        build();

        Analysis<String, double[]> analysis = getAnalysis(analyzerName, line);
        String url = getUrl(Analyzer.ANALYZE);
        Analysis result = HttpClientUtilities.doPost(url, analysis, Analysis.class);
        assertTrue(Integer.parseInt(result.getClazz()) >= 0 && Integer.parseInt(result.getClazz()) <= 6);
    }

    @Test
    public void destroy() throws Exception {
        analyze();

        Analysis analysis = getAnalysis(analyzerName, null);
        String url = getUrl(Analyzer.CONTEXT);
        Context context = HttpClientUtilities.doPost(url, analysis, Context.class);

        assertNotNull(context);

        String destroyUrl = getUrl(Analyzer.DESTROY);
        HttpClientUtilities.doPost(destroyUrl, context, Context.class);

        context = HttpClientUtilities.doPost(url, analysis, Context.class);
        assertNull(context);
    }

    @Test
    public void context() throws Exception {
        analyze();

        Analysis analysis = getAnalysis(analyzerName, null);
        String url = getUrl(Analyzer.CONTEXT);
        Context context = HttpClientUtilities.doPost(url, analysis, Context.class);
        assertNotNull(context);
    }

    @Test
    public void contexts() throws Exception {
        analyze();

        String contextsUrl = getUrl(Analyzer.CONTEXTS);
        String[] names = HttpClientUtilities.doGet(contextsUrl, String[].class);
        List<String> list = new ArrayList<>(Arrays.asList(names));
        assertTrue(list.contains(analyzerName));
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getUrl(final String service) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(IConstants.SEP);
        builder.append(IConstants.IKUBE);
        builder.append(AbstractTest.SERVICE);
        builder.append(Analyzer.ANALYZER);
        builder.append(service);
        return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
    }

}