package ikube.action.index.handler.internet;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import mockit.Deencapsulation;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Pattern;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class InternetResourceProviderTest extends AbstractTest {

    private IndexableInternet indexableInternet;
    private InheritableThreadLocal<Stack<Url>> urls;
    private InternetResourceProvider internetResourceProvider;

    @Before
    public void before() {
        internetResourceProvider = new InternetResourceProvider();

        indexableInternet = mock(IndexableInternet.class);
        when(indexableInternet.getThreads()).thenReturn(10);
        when(indexableInternet.getName()).thenReturn("indexable-internet");
        when(indexableInternet.getUrl()).thenReturn("http://www.eacbs.com/");
        when(indexableInternet.getBaseUrl()).thenReturn("http://www.eacbs.com/");
        when(indexableInternet.getExcludedPattern()).thenReturn("zip");

        urls = new InheritableThreadLocal<>();
        urls.set(new Stack<Url>());
    }

    @After
    public void after() {
        if (StringUtils.isNotEmpty(indexableInternet.getName())) {
            FileUtilities.deleteFile(new File("./" + indexableInternet.getName()));
        }
    }

    @Test
    public void getResource() {
        urls.get().add(new Url());
        Deencapsulation.setField(internetResourceProvider, "URLS", urls);

        Url resourceUrl = internetResourceProvider.getResource();
        assertNotNull(resourceUrl);

        resourceUrl = internetResourceProvider.getResource();
        assertNull(resourceUrl);
    }

    @Test
    @SuppressWarnings("StatementWithEmptyBody")
    public void setResources() {
        Deencapsulation.setField(internetResourceProvider, "URLS", urls);

        internetResourceProvider.setResources(Arrays.asList(new Url()));
        assertTrue(urls.get().size() > 0);
    }

    @Test
    public void initialize() {
        internetResourceProvider.initialize(indexableInternet);
        verify(indexableInternet, atLeastOnce()).getUrl();
    }


    @Test
    public void shouldVisit() {
        WebURL webUrl = mock(WebURL.class);
        when(webUrl.getURL()).thenReturn("http://www.eacbs.com/");
        InheritableThreadLocal<Pattern> pattern = new InheritableThreadLocal<>();
        pattern.set(Pattern.compile("some-pattern"));
        Deencapsulation.setField(internetResourceProvider, "PATTERN", pattern);

        InheritableThreadLocal<IndexableInternet> indexableInternet = new InheritableThreadLocal<>();
        indexableInternet.set(this.indexableInternet);
        Deencapsulation.setField(internetResourceProvider, "INDEXABLE_INTERNET", indexableInternet);

        boolean shouldVisit = internetResourceProvider.shouldVisit(webUrl);
        assertTrue(shouldVisit);

        pattern.set(Pattern.compile(".*(eacbs).*"));
        shouldVisit = internetResourceProvider.shouldVisit(webUrl);
        assertFalse(shouldVisit);

        pattern.set(Pattern.compile("some-pattern"));
        shouldVisit = internetResourceProvider.shouldVisit(webUrl);
        assertTrue(shouldVisit);

        when(webUrl.getURL()).thenReturn("http://www.google.com/");
        shouldVisit = internetResourceProvider.shouldVisit(webUrl);
        assertFalse(shouldVisit);
    }

    @Test
    public void visit() {
        when(indexableInternet.getMaxReadLength()).thenReturn(100l);
        InheritableThreadLocal<IndexableInternet> indexableInternet = new InheritableThreadLocal<>();
        indexableInternet.set(this.indexableInternet);

        Page page = mock(Page.class);
        byte[] bytes = new byte[1024];
        Arrays.fill(bytes, (byte) 1);
        when(page.getContentData()).thenReturn(bytes);
        when(page.getContentType()).thenReturn(IConstants.ENCODING);

        Deencapsulation.setField(internetResourceProvider, "URLS", urls);
        Deencapsulation.setField(internetResourceProvider, "INDEXABLE_INTERNET", indexableInternet);

        internetResourceProvider.visit(page);
        Url url = urls.get().get(0);
        assertEquals(indexableInternet.get().getMaxReadLength(), url.getRawContent().length);
    }

}