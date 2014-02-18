package ikube.action.index.handler.internet;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @author Michael Couck
 * @version 02.00
 * @since 18-02-2014
 */
public class InternetWebCrawler extends WebCrawler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    IndexableInternet indexableInternet;
    Pattern pattern;
    Stack<Url> urls;

    public InternetWebCrawler(IndexableInternet indexableInternet, Pattern pattern, Stack<Url> urls) {
        this.indexableInternet = indexableInternet;
        this.pattern = pattern;
        this.urls = urls;
    }

    @Override
    public boolean shouldVisit(final WebURL url) {
        String href = url.getURL().toLowerCase();
        boolean excluded = pattern.matcher(href).matches();
        boolean sameDomain = href.startsWith(indexableInternet.getBaseUrl());
        boolean shouldVisit = !excluded && sameDomain;
        if (logger.isDebugEnabled()) {
            logger.info("Visiting : " + shouldVisit + ", " + href);
        }
        return shouldVisit;
    }

    @Override
    public void visit(final Page page) {
        byte[] rawContent = page.getContentData();

        if (rawContent != null && rawContent.length > indexableInternet.getMaxReadLength()) {
            byte[] newRawContent = new byte[(int) indexableInternet.getMaxReadLength()];
            System.arraycopy(rawContent, 0, newRawContent, 0, newRawContent.length);
            rawContent = newRawContent;
        }

        Url url = new Url();
        url.setIndexed(Boolean.FALSE);
        url.setRawContent(rawContent);
        url.setContentType(page.getContentType());

        urls.add(url);
    }
}
