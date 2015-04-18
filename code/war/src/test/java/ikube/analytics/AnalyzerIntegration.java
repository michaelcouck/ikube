package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.THREAD;
import ikube.web.service.Analyzer;
import org.junit.After;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static ikube.toolkit.REST.doPost;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-09-2014
 */
public abstract class AnalyzerIntegration extends AbstractTest {

    @After
    public void after() {
        try {
            Context context = getContext();
            String destroyUri = getAnalyzerRestUri(Analyzer.DESTROY);
            doPost(destroyUri, context, Context.class);
            THREAD.sleep(15000);
        } catch (final Exception e) {
            logger.error("Exception destroying the analyzer : ", e);
        }
    }

    protected abstract Context getContext();

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        Context context = getContext();
        String createUri = getAnalyzerRestUri(Analyzer.CREATE);
        logger.warn(createUri + ":" + IConstants.GSON.toJson(context));
        Context result = doPost(createUri, context, Context.class);

        assertTrue(result.getAlgorithms().length > 0);
        assertNotNull(result.getAnalyzer());
        for (int i = 0; i < result.getAlgorithms().length; i++) {
            assertNotNull(result.getAlgorithms()[i]);
        }
    }

    protected abstract void train() throws Exception;

    @Test
    public void build() throws Exception {
        train();

        Analysis analysis = new Analysis();
        analysis.setContext(getContext().getName());

        String buildUri = getAnalyzerRestUri(Analyzer.BUILD);
        logger.warn(buildUri + ":" + IConstants.GSON.toJson(analysis));
        Context context = doPost(buildUri, analysis, Context.class);
        assertTrue(context.isBuilt());
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    protected String getAnalyzerRestUri(final String service) throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(IConstants.SEP);
        builder.append(IConstants.IKUBE);
        builder.append(AbstractTest.SERVICE);
        builder.append(Analyzer.ANALYZER);
        builder.append(service);
        return new URL("http", LOCALHOST, SERVER_PORT, builder.toString()).toString();
    }

}