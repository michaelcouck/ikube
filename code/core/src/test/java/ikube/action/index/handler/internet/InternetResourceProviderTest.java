package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class InternetResourceProviderTest extends AbstractTest {

    @Mock
    private IndexableInternet indexableInternet;
    private InternetResourceProvider internetResourceProvider;

    @Before
    @SuppressWarnings("unchecked")
    public void before() {
        when(indexableInternet.getThreads()).thenReturn(10);
        when(indexableInternet.getName()).thenReturn("indexable-internet");
        when(indexableInternet.getUrl()).thenReturn("http://www.ikube.be/site/");
        when(indexableInternet.getExcludedPattern()).thenReturn("zip");
        when(indexableInternet.getParent()).thenReturn(indexContext);

        internetResourceProvider = new InternetResourceProvider(indexableInternet, dataBase) {
            void initialize(final IndexableInternet indexableInternet) {
                // Do nothing
            }
        };
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void initialize() {
        internetResourceProvider.initialize(indexableInternet);
        ThreadUtilities.sleep(3000);
        verify(indexContext, atLeastOnce()).getName();
    }

    @Test
    public void setResources() {
        Url url = new Url();
        internetResourceProvider.setResources(Arrays.asList(url));
        Stack<Url> urls = Deencapsulation.getField(internetResourceProvider, "urls");
        assertNotNull(urls);
        assertEquals(1, urls.size());

        //noinspection StatementWithEmptyBody
        while (internetResourceProvider.getResource() != null) ;

        urls = new Stack<>();
        Url[] array = new Url[1001];
        Arrays.fill(array, 0, array.length, url);
        urls.addAll(Arrays.asList(array));
        internetResourceProvider.setResources(urls);
        verify(dataBase, atLeastOnce()).persistBatch(any(List.class));
    }

    @Test
    public void getResource() {
        Stack<Url> urls = new Stack<>();
        urls.push(new Url());
        Deencapsulation.setField(internetResourceProvider, "urls", urls);
        Deencapsulation.setField(internetResourceProvider, "RETRY", 1);
        Deencapsulation.setField(internetResourceProvider, "SLEEP", 100);
        Url url = internetResourceProvider.getResource();
        assertNotNull(url);
        //noinspection ConstantConditions
        do {
            // Deplete the urls
            url = internetResourceProvider.getResource();
        } while (url != null);
        assertNull(internetResourceProvider.getResource());
    }

}