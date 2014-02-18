package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import mockit.Deencapsulation;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class InternetResourceProviderTest extends AbstractTest {

    private Stack<Url> urls;
    private IndexableInternet indexableInternet;
    private InternetResourceProvider internetResourceProvider;

    @Before
    public void before() {
        indexableInternet = mock(IndexableInternet.class);

        when(indexableInternet.getThreads()).thenReturn(10);
        when(indexableInternet.getName()).thenReturn("indexable-internet");
        when(indexableInternet.getUrl()).thenReturn("http://www.eacbs.com/");
        when(indexableInternet.getBaseUrl()).thenReturn("http://www.eacbs.com/");
        when(indexableInternet.getExcludedPattern()).thenReturn("zip");

        internetResourceProvider = new InternetResourceProvider(indexableInternet);
        urls = new Stack<>();
    }

    @After
    public void after() {
        if (StringUtils.isNotEmpty(indexableInternet.getName())) {
            FileUtilities.deleteFile(new File("./" + indexableInternet.getName()));
        }
    }

    @Test
    public void getResource() {
        urls.add(new Url());
        Deencapsulation.setField(internetResourceProvider, "urls", urls);

        Url resourceUrl = internetResourceProvider.getResource();
        assertNotNull(resourceUrl);

        Url url;
        do {
            url = internetResourceProvider.getResource();
        } while (url != null);
        resourceUrl = internetResourceProvider.getResource();
        assertNull(resourceUrl);
    }

    @Test
    @SuppressWarnings("StatementWithEmptyBody")
    public void setResources() {
        Deencapsulation.setField(internetResourceProvider, "urls", urls);

        internetResourceProvider.setResources(Arrays.asList(new Url()));
        assertTrue(urls.size() > 0);
    }

    @Test
    public void initialize() {
        internetResourceProvider.initialize(indexableInternet);
        ThreadUtilities.sleep(15000);
        verify(indexableInternet, atLeastOnce()).getUrl();
    }


}