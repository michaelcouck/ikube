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
import org.niocchi.core.Query;
import org.niocchi.core.URLPoolException;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class InternetResourceProviderTest extends AbstractTest {

    private IndexableInternet indexableInternet;
    private InternetResourceProvider internetResourceProvider;

    @Before
    public void before() {
        indexableInternet = mock(IndexableInternet.class);

        when(indexableInternet.getThreads()).thenReturn(10);
        when(indexableInternet.getName()).thenReturn("indexable-internet");
        when(indexableInternet.getUrl()).thenReturn("http://www.eacbs.com/");
        // when(indexableInternet.getBaseUrl()).thenReturn("http://www.eacbs.com/");
        when(indexableInternet.getExcludedPattern()).thenReturn("zip");

        internetResourceProvider = new InternetResourceProvider(indexableInternet, dataBase);
        Deencapsulation.setField(internetResourceProvider, "RETRY", 1);
        Deencapsulation.setField(internetResourceProvider, "SLEEP", 1000);
    }

    @After
    public void after() {
        if (StringUtils.isNotEmpty(indexableInternet.getName())) {
            FileUtilities.deleteFile(new File("./" + indexableInternet.getName()));
        }
    }

    @Test
    public void initialize() {
        internetResourceProvider.initialize(indexableInternet);
        ThreadUtilities.sleep(5000);
        verify(indexableInternet, atLeastOnce()).getUrl();
    }

    @Test
    public void setResources() {
        Url url = new Url();
        url.setUrl("www.google.com");
        internetResourceProvider.setResources(Arrays.asList(url));
        TreeSet<Url> urls = Deencapsulation.getField(internetResourceProvider, "urls");
        assertEquals(1, urls.size());
    }

    @Test
    public void getResource() {
        Url url = new Url();
        url.setUrl("www.google.com");
        TreeSet<Url> urls = new TreeSet<>(new Comparator<Url>() {
            @Override
            public int compare(final Url o1, final Url o2) {
                return o1.getUrl().compareTo(o2.getUrl());
            }
        });
        urls.add(url);

        Deencapsulation.setField(internetResourceProvider, "urls", urls);

        Url resourceUrl = internetResourceProvider.getResource();
        assertNotNull(resourceUrl);
        resourceUrl = internetResourceProvider.getResource();
        assertNull(resourceUrl);
        assertEquals(0, urls.size());
    }

    @Test
    public void hasNextQuery() {
        boolean hasNextQuery = internetResourceProvider.hasNextQuery();
        assertFalse(hasNextQuery);
        when(dataBase.find(any(Class.class), any(String[].class), any(Object[].class))).thenReturn(new Url());
        hasNextQuery = internetResourceProvider.hasNextQuery();
        assertTrue(hasNextQuery);
    }

    @Test
    public void getNextQuery() throws Exception {
        Query query = internetResourceProvider.getNextQuery();
        assertNull(query);
        Url url = new Url();
        url.setUrl("http://ikube.be/ikube/login.html#anchor;jsessionid=C2940CFC3599CF62ACFFED627DAD6CD4");
        when(dataBase.find(any(Class.class), any(String[].class), any(Object[].class))).thenReturn(url);
        query = internetResourceProvider.getNextQuery();
        assertNotNull(query);
        assertEquals("http://ikube.be/ikube/login.html", query.getURL().toExternalForm());
    }

}