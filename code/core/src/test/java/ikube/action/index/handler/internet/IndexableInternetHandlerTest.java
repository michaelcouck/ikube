package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ThreadUtilities;
import mockit.MockClass;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
@Ignore
public class IndexableInternetHandlerTest extends AbstractTest {

    @MockClass(realClass = InternetResourceProvider.class)
    public static class InternetResourceProviderMock {
        @mockit.Mock
        public void $init(final IndexableInternet indexableInternet, final IDataBase dataBase) {
            // Do nothing
        }
    }

    @Mock
    private IndexableInternet indexableInternet;
    @Mock
    private InternetResourceHandler internetResourceHandler;
    @Spy
    @InjectMocks
    private IndexableInternetHandler indexableInternetHandler;

    @Before
    public void before() {
        Mockit.setUpMock(InternetResourceProviderMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(InternetResourceProviderMock.class);
    }

    @Test
    public void handleIndexable() throws Exception {
        when(indexableInternet.getUrl()).thenReturn("http://www.google.com:8080");
        ForkJoinTask<?> forkJoinTask = indexableInternetHandler.handleIndexableForked(indexContext, indexableInternet);
        ThreadUtilities.waitForFuture(forkJoinTask, 10);
        assertNotNull(forkJoinTask);
    }

    @Test
    public void handleResource() throws Exception {
        Url url = mock(Url.class);
        when(url.getRawContent()).thenReturn("hello world".getBytes());
        indexableInternetHandler.handleResource(indexContext, indexableInternet, url);
        verify(internetResourceHandler, atLeastOnce())
            .handleResource(any(IndexContext.class), any(IndexableInternet.class), any(Document.class), any(Object.class));
    }

}