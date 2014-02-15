package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class IndexableInternetHandlerTest extends AbstractTest {

    private String url = "http://www.ikube.be/ikube";
    private IndexableInternet indexableInternet;
    private IndexableInternetHandler indexableInternetHandler;

    @Before
    public void before() {
        ThreadUtilities.initialize();

        indexableInternet = new IndexableInternet();
        indexableInternet.setIdFieldName(IConstants.ID);
        indexableInternet.setTitleFieldName(IConstants.TITLE);
        indexableInternet.setContentFieldName(IConstants.CONTENT);

        indexableInternet.setThreads(3);
        indexableInternet.setUrl(url);
        indexableInternet.setBaseUrl(url);
        indexableInternet.setMaxReadLength(Integer.MAX_VALUE);
        indexableInternet.setExcludedPattern("some-pattern");
        indexableInternet.setParent(indexableInternet);

        indexableInternetHandler = new IndexableInternetHandler();

        InternetResourceHandler resourceHandler = new InternetResourceHandler() {
            public Document handleResource(
                    final IndexContext<?> indexContext,
                    final IndexableInternet indexable,
                    final Document document,
                    final Object resource)
                    throws Exception {
                return document;
            }
        };
        Deencapsulation.setField(indexableInternetHandler, resourceHandler);
    }

    @Test
    public void handleIndexable() throws Exception {
        ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
        assertNotNull(forkJoinTask);
    }

    @Test
    public void handleResource() {
        Url url = mock(Url.class);

        when(url.getUrl()).thenReturn(this.url);
        when(url.getParsedContent()).thenReturn("text/html");
        when(url.getRawContent()).thenReturn("hello world".getBytes());

        indexableInternetHandler.handleResource(indexContext, indexableInternet, url);
        verify(url, atLeastOnce()).setParsedContent(anyString());
    }

}