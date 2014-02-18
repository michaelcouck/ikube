package ikube.action.index.handler.internet;

import edu.uci.ics.crawler4j.CrawlControllerExt;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
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
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @author Michael Couck
 * @version 02.00
 * @since 21-06-2013
 */
public class InternetResourceProvider implements IResourceProvider<Url> {

    Logger logger = LoggerFactory.getLogger(InternetResourceProvider.class);

    Stack<Url> urls;

    public InternetResourceProvider(final IndexableInternet indexableInternet) {
        initialize(indexableInternet);
    }

    void initialize(final IndexableInternet indexableInternet) {
        urls = new Stack<>();
        final Pattern pattern = Pattern.compile(indexableInternet.getExcludedPattern());

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

        final CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(Short.MAX_VALUE);
        config.setMaxPagesToFetch(Integer.MAX_VALUE);
        config.setResumableCrawling(false);

        final PageFetcher pageFetcher = new PageFetcher(config);
        final RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        final RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        ThreadUtilities.submit(indexableInternet.getName(), new Runnable() {
            public void run() {
                final CrawlController controller;
                try {
                    controller = new CrawlControllerExt(config, pageFetcher, robotstxtServer, indexableInternet, pattern, urls);
                    controller.addSeed(indexableInternet.getUrl());
                    controller.start(InternetWebCrawler.class, indexableInternet.getThreads());
                    logger.info("Returning from starting crawl : " + Thread.currentThread().hashCode());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    ThreadUtilities.destroy(indexableInternet.getName());
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Url getResource() {
        if (urls.isEmpty()) {
            // We'll wait a few seconds to see if any other thread will add some URLS to the stack
            ThreadUtilities.sleep(10000);
            if (urls.isEmpty()) {
                return null;
            } else {
                return getResource();
            }
        }
        // logger.info("Popping : " + urls.peek());
        return urls.pop();
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
            if (urls.size() < IConstants.MILLION) {
                urls.push(url);
            }
        }
    }

}