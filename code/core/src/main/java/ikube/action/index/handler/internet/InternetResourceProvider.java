package ikube.action.index.handler.internet;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @author Michael Couck
 * @version 02.00
 * @since 21-06-2013
 */
public class InternetResourceProvider extends WebCrawler implements IResourceProvider<Url> {

    Logger logger = LoggerFactory.getLogger(InternetResourceProvider.class);

    static InheritableThreadLocal<Stack<Url>> URLS;
    static InheritableThreadLocal<Pattern> PATTERN;
    static InheritableThreadLocal<IndexableInternet> INDEXABLE_INTERNET;

    public InternetResourceProvider() {
    }

    public InternetResourceProvider(final IndexableInternet indexableInternet) {
        initialize(indexableInternet);
    }

    void initialize(final IndexableInternet indexableInternet) {
        InternetResourceProvider.URLS = new InheritableThreadLocal<>();
        InternetResourceProvider.INDEXABLE_INTERNET = new InheritableThreadLocal<>();
        InternetResourceProvider.PATTERN = new InheritableThreadLocal<>();

        InternetResourceProvider.URLS.set(new Stack<Url>());
        InternetResourceProvider.INDEXABLE_INTERNET.set(indexableInternet);
        InternetResourceProvider.PATTERN.set(Pattern.compile(indexableInternet.getExcludedPattern()));

        String folderName = indexableInternet.getName();
        if (StringUtils.isEmpty(folderName)) {
            folderName = indexableInternet.getParent().getName();
            if (StringUtils.isEmpty(folderName)) {
                folderName = Integer.toString(this.hashCode());
                logger.warn("Couldn't get folder for output, delete this folder after the crawl : " + folderName);
            }
        }
        File file = new File("./" + folderName);
        // This is a sanity check that we don't delete the dor folder!
        if (StringUtils.isNotEmpty(folderName)) {
            logger.info("Deleting folder for crawl : " + file.getAbsolutePath());
            FileUtilities.deleteFile(file);
        }
        FileUtilities.getOrCreateDirectory(file);
        String crawlStorageFolder = FileUtilities.cleanFilePath(file.getAbsolutePath());

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(Short.MAX_VALUE);
        config.setMaxPagesToFetch(Integer.MAX_VALUE);
        config.setResumableCrawling(false);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        try {
            final CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            controller.addSeed(indexableInternet.getUrl());
            controller.start(InternetResourceProvider.class, indexableInternet.getThreads());
            logger.info("Returning from starting crawl : " + Thread.currentThread().hashCode());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean shouldVisit(final WebURL url) {
        String href = url.getURL().toLowerCase();
        boolean excluded = PATTERN.get().matcher(href).matches();
        boolean sameDomain = href.startsWith(INDEXABLE_INTERNET.get().getBaseUrl());
        boolean shouldVisit = !excluded && sameDomain;
        if (logger.isDebugEnabled()) {
            logger.info("Visiting : " + shouldVisit + ", " + href);
        }
        return shouldVisit;
    }

    @Override
    public void visit(final Page page) {
        byte[] rawContent = page.getContentData();

        if (rawContent != null && rawContent.length > INDEXABLE_INTERNET.get().getMaxReadLength()) {
            byte[] newRawContent = new byte[(int) INDEXABLE_INTERNET.get().getMaxReadLength()];
            System.arraycopy(rawContent, 0, newRawContent, 0, newRawContent.length);
            rawContent = newRawContent;
        }

        Url url = new Url();
        url.setIndexed(Boolean.FALSE);
        url.setRawContent(rawContent);
        url.setContentType(page.getContentType());

        setResources(Arrays.asList(url));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Url getResource() {
        if (URLS.get().isEmpty()) {
            // We'll wait a few seconds to see if any other thread will add some URLS to the stack
            ThreadUtilities.sleep(10000);
            if (URLS.get().isEmpty()) {
                return null;
            } else {
                return getResource();
            }
        }
        return URLS.get().pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResources(final List<Url> resources) {
        if (resources == null) {
            return;
        }
        for (final Url url : resources) {
            if (URLS.get().size() < IConstants.MILLION) {
                URLS.get().push(url);
            }
        }
    }

}