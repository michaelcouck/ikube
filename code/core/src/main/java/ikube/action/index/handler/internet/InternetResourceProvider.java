package ikube.action.index.handler.internet;

import com.googlecode.flaxcrawler.CrawlerConfiguration;
import com.googlecode.flaxcrawler.CrawlerController;
import com.googlecode.flaxcrawler.CrawlerException;
import com.googlecode.flaxcrawler.DefaultCrawler;
import com.googlecode.flaxcrawler.download.DefaultDownloaderController;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;
import com.googlecode.flaxcrawler.parse.DefaultParserController;
import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.database.IDataBase;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Stack;

/**
 * This class provides urls for the {@link ikube.action.index.handler.internet.IndexableInternetHandler}. It
 * acts as a crawler for web pages and other web resources. The underlying crawler is FlaxCrawler. Several threads are
 * started and feed the database with {@link ikube.model.Url}s.
 * <p/>
 * Consumer threads are started by the handler, that request resources, i.e. {@link ikube.model.Url}s from this provider.
 *
 * @author Michael Couck
 * @version 03.00
 * @since 21-06-2013
 */
public class InternetResourceProvider implements IResourceProvider<Url> {

    private static int RETRY = 5;
    private static long SLEEP = 6000;

    private Logger logger = LoggerFactory.getLogger(InternetResourceProvider.class);

    private Stack<Url> urls;
    private IDataBase dataBase;
    private IndexableInternet indexableInternet;

    public InternetResourceProvider(final IndexableInternet indexableInternet, final IDataBase dataBase) {
        initialize(indexableInternet);
        this.indexableInternet = indexableInternet;
        this.dataBase = dataBase;
    }

    void initialize(final IndexableInternet indexableInternet) {
        urls = new Stack<>();
        DefaultDownloaderController downloaderController = new DefaultDownloaderController();
        // Setting up parser controller
        DefaultParserController parserController = new DefaultParserController();

        // Creating crawler configuration object
        CrawlerConfiguration configuration = new CrawlerConfiguration();

        // Creating some crawlers. The fetching is much faster thatn the indexing
        // so we create many more threads to index than to fetch so we don't build up
        // urls on the stack
        int crawlers = Math.max(1, indexableInternet.getThreads() / 10);
        for (int i = 0; i < crawlers; i++) {
            // Creating crawler and setting downloader and parser controllers
            DefaultCrawler crawler = new DefaultCrawler() {
                @Override
                protected void afterCrawl(final CrawlerTask crawlerTask, final Page page) {
                    super.afterCrawl(crawlerTask, page);
                    logger.debug("After crawl : " + urls.size() + ", " + page);
                    if (page != null) {
                        Url url = getUrl(page.getUrl().toExternalForm(), indexableInternet);
                        url.setRawContent(page.getContent());
                        url.setContentType(page.getHeader("Content-Type"));
                        if (urls.size() < IConstants.ONE_THOUSAND) {
                            urls.push(url);
                        } else {
                            // If we go over the limit for the stack size then we need to
                            // persist the url in the database to avoid running out of memory
                            dataBase.persist(url);
                        }
                    }
                }
            };
            crawler.setDownloaderController(downloaderController);
            crawler.setParserController(parserController);
            // Adding crawler to the configuration object
            configuration.addCrawler(crawler);
        }

        // Setting maximum parallel requests to a single site limit
        configuration.setMaxParallelRequests(crawlers);
        // Setting http errors limits. If this limit violated for any
        // site - crawler will stop this site processing
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_CLIENT_TIMEOUT, (int) indexableInternet.getMaxExceptions());
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_BAD_GATEWAY, (int) indexableInternet.getMaxExceptions());
        // Setting period between two requests to a single site (in milliseconds)
        configuration.setPolitenessPeriod(0);

        // Initializing crawler controller
        final CrawlerController crawlerController = new CrawlerController(configuration);
        ThreadUtilities.submit(indexableInternet.getName(), new Runnable() {
            public void run() {
                try {
                    // Adding crawler seed
                    crawlerController.addSeed(new URL(indexableInternet.getUrl()));
                    logger.info("Starting crawl : ");
                    crawlerController.start();
                    // Join crawler controller and wait for finish
                    crawlerController.join(Integer.MAX_VALUE);
                    // Stopping crawler controller
                    // crawlerController.stop();
                    // TODO: Some termination code here...
                } catch (final MalformedURLException e) {
                    logger.error("Bad url : ", e);
                } catch (final CrawlerException e) {
                    logger.error("Crawler exception : ", e);
                } finally {
                    try {
                        logger.info("Terminating crawler : " + indexableInternet.getName());
                        crawlerController.stop();
                    } catch (final CrawlerException e) {
                        logger.error("Crawl terminated exception : ", e);
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Url getResource() {
        int retry = RETRY;
        Url url = !urls.isEmpty() ? urls.pop() : null;
        while (url == null && retry-- >= 0) {
            logger.info("Sleeping : " + urls.size());
            ThreadUtilities.sleep(SLEEP);
            if (urls.isEmpty()) {
                continue;
            }
            url = urls.pop();
        }
        // If there are not urls on the stack try the database
        if (url == null) {
            logger.info("Going to database for resources : ");
            String[] fields = {IConstants.NAME, IConstants.INDEXED};
            Object[] values = {indexableInternet.getName(), Boolean.FALSE};
            url = dataBase.find(Url.class, fields, values);
            if (url != null) {
                logger.info("Removing url : " + url);
                dataBase.remove(url);
            }
        }
        logger.debug("Doing url : " + url + ", " + urls.size());
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setResources(final List<Url> resources) {
        urls.addAll(resources);
    }

    private Url getUrl(final String stringUrl, final IndexableInternet indexableInternet) {
        Url url = new Url();
        url.setIndexed(Boolean.FALSE);
        url.setName(indexableInternet.getName());
        url.setUrl(stringUrl);
        return url;
    }

}