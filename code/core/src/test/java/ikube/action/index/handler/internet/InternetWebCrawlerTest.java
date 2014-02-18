package ikube.action.index.handler.internet;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Pattern;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-06-2013
 */
public class InternetWebCrawlerTest extends AbstractTest {

    private Stack<Url> urls;
    private IndexableInternet indexableInternet;
    private InternetWebCrawler internetWebCrawler;

    @Before
    public void before() {
        indexableInternet = mock(IndexableInternet.class);

        when(indexableInternet.getThreads()).thenReturn(10);
        when(indexableInternet.getName()).thenReturn("indexable-internet");
        when(indexableInternet.getUrl()).thenReturn("http://www.eacbs.com/");
        when(indexableInternet.getBaseUrl()).thenReturn("http://www.eacbs.com/");
        when(indexableInternet.getExcludedPattern()).thenReturn("zip");

        Pattern pattern = Pattern.compile("some-pattern");
        internetWebCrawler = new InternetWebCrawler(indexableInternet, pattern, urls);
        urls = new Stack<>();
    }

    @Test
    public void shouldVisit() {
        WebURL webUrl = mock(WebURL.class);
        when(webUrl.getURL()).thenReturn("http://www.eacbs.com/");
        Pattern pattern = Pattern.compile("some-pattern");
        Deencapsulation.setField(internetWebCrawler, "pattern", pattern);

        boolean shouldVisit = internetWebCrawler.shouldVisit(webUrl);
        assertTrue(shouldVisit);

        pattern = Pattern.compile(".*(eacbs).*");
        Deencapsulation.setField(internetWebCrawler, "pattern", pattern);
        shouldVisit = internetWebCrawler.shouldVisit(webUrl);
        assertFalse(shouldVisit);

        pattern = Pattern.compile("some-pattern");
        Deencapsulation.setField(internetWebCrawler, "pattern", pattern);
        shouldVisit = internetWebCrawler.shouldVisit(webUrl);
        assertTrue(shouldVisit);

        when(webUrl.getURL()).thenReturn("http://www.google.com/");
        shouldVisit = internetWebCrawler.shouldVisit(webUrl);
        assertFalse(shouldVisit);
    }

    @Test
    public void visit() {
        when(indexableInternet.getMaxReadLength()).thenReturn(100l);

        Page page = mock(Page.class);
        byte[] bytes = new byte[1024];
        Arrays.fill(bytes, (byte) 1);
        when(page.getContentData()).thenReturn(bytes);
        when(page.getContentType()).thenReturn(IConstants.ENCODING);

        Deencapsulation.setField(internetWebCrawler, "urls", urls);
        Deencapsulation.setField(internetWebCrawler, "indexableInternet", indexableInternet);

        internetWebCrawler.visit(page);
        Url url = urls.get(0);
        assertEquals(indexableInternet.getMaxReadLength(), url.getRawContent().length);
    }

}
