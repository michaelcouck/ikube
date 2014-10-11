package ikube.action.index.handler.internet;

import com.googlecode.flaxcrawler.CrawlerConfiguration;
import com.googlecode.flaxcrawler.CrawlerController;
import com.googlecode.flaxcrawler.CrawlerException;
import com.googlecode.flaxcrawler.DefaultCrawler;
import com.googlecode.flaxcrawler.download.DefaultDownloader;
import com.googlecode.flaxcrawler.download.DefaultDownloaderController;
import com.googlecode.flaxcrawler.download.Downloader;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;
import com.googlecode.flaxcrawler.parse.DefaultParserController;
import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.ThreadUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

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
@SuppressWarnings("FieldCanBeLocal")
public class InternetResourceProvider implements IResourceProvider<Url> {

    private static int RETRY = 3;
    private static long SLEEP = 10000;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Random random = new Random();
    private Stack<Url> urls = new Stack<>();
    private IDataBase dataBase;
    private IndexableInternet indexableInternet;
    private boolean terminated;

    public InternetResourceProvider(final IndexableInternet indexableInternet, final IDataBase dataBase) {
        initialize(indexableInternet);
        this.indexableInternet = indexableInternet;
        this.dataBase = dataBase;
    }

    void initialize(final IndexableInternet indexableInternet) {
        final Pattern pattern;
        if (indexableInternet.getExcludedPattern() == null) {
            pattern = null;
        } else {
            pattern = Pattern.compile(indexableInternet.getExcludedPattern());
        }

        DefaultParserController parserController = new DefaultParserController();
        DefaultDownloaderController downloaderController = new DefaultDownloaderController() {

            @Override
            @SuppressWarnings("UnnecessaryLocalVariable")
            public Downloader getDownloader(final URL url) {
                DefaultDownloader defaultDownloader = (DefaultDownloader) super.getDownloader(url);
                // Collection<String> mimeTypes = MimeTypes.getTypesCollection();
                // mimeTypes.toArray(new String[mimeTypes.size()])
                defaultDownloader.setAllowedContentTypes(null);
                // NOTE: If you set this then nothing is downloaded!!!!!! WTF?
                // defaultDownloader.setMaxContentLength(Long.MAX_VALUE);
                defaultDownloader.setTriesCount(3);
                return defaultDownloader;
            }

        };

        // Creating crawler configuration object
        CrawlerConfiguration configuration = new CrawlerConfiguration();
        // Creating some crawlers. The fetching is much faster than the indexing
        // so we persist the excess urls in the database so we don't build up
        // urls on the stack
        for (int i = 0; i < indexableInternet.getThreads(); i++) {
            // Creating crawler and setting downloader and parser controllers
            DefaultCrawler crawler = new DefaultCrawler() {

                @Override
                protected void afterCrawl(final CrawlerTask crawlerTask, final Page page) {
                    super.afterCrawl(crawlerTask, page);
                    logger.error("After crawl : " + urls.size() + ", " + page);
                    if (page != null) {
                        Url url = getUrl(page.getUrl().toExternalForm(), indexableInternet);
                        url.setRawContent(page.getContent());
                        url.setContentType(page.getHeader("Content-Type"));
                        setResources(Arrays.asList(url));
                    }
                }

                @Override
                public boolean shouldCrawl(final CrawlerTask crawlerTask, final CrawlerTask parent) {
                    if (isTerminated()) {
                        return Boolean.FALSE;
                    }
                    boolean excluded = pattern != null ? pattern.matcher(crawlerTask.getUrl()).matches() : Boolean.FALSE;
                    boolean shouldCrawl = super.shouldCrawl(crawlerTask, parent) && !excluded;
                    logger.debug("Should crawl : " + shouldCrawl);
                    return shouldCrawl;
                }

            };
            crawler.setParserController(parserController);
            crawler.setDownloaderController(downloaderController);
            // Adding crawler to the configuration object
            configuration.addCrawler(crawler);
        }

        configuration.setMaxLevel(Integer.MAX_VALUE);
        // Setting maximum parallel requests to a single site limit
        configuration.setMaxParallelRequests(indexableInternet.getThreads());
        // Setting http errors limits. If this limit violated for any
        // site - crawler will stop this site processing
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_BAD_GATEWAY, (int) indexableInternet.getMaxExceptions());
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_CLIENT_TIMEOUT, (int) indexableInternet.getMaxExceptions());
        // Setting period between two requests to a single site (in milliseconds)
        configuration.setPolitenessPeriod((int) getIndexContext(indexableInternet).getThrottle());

        // Initializing crawler controller
        final CrawlerController crawlerController = new CrawlerController(configuration);

        class CrawlerExecutor extends RecursiveAction {
            @Override
            protected void compute() {
                try {
                    // Adding crawler seed
                    crawlerController.addSeed(new URL(indexableInternet.getUrl()));
                    logger.info("Starting crawl : ");
                    crawlerController.start();
                    // Wait for the crawler controller either to finish, and
                    // have no more urls, or for the fork join pool to get cancelled
                    do {
                        if (isDone() ||
                                isCancelled() ||
                                isCompletedNormally() ||
                                isCompletedAbnormally()) {
                            terminateCrawler();
                            break;
                        }
                        logger.debug("Thread finished : " +
                                ", done : " + isDone() +
                                ", cancelled : " + isCancelled() +
                                ", completed normally : " + isCompletedNormally() +
                                ", completed abnormally : " + isCompletedAbnormally());
                        // Join crawler controller and wait for finish
                        crawlerController.join(IConstants.ONE_THOUSAND);
                    } while (true);
                } catch (final MalformedURLException e) {
                    logger.error("Bad url : ", e);
                } catch (final CrawlerException e) {
                    logger.error("Crawler exception : ", e);
                } finally {
                    terminateCrawler();
                }
            }

            private void terminateCrawler() {
                try {
                    logger.error("Terminating crawler : " + indexableInternet.getName());
                    crawlerController.dispose();
                    crawlerController.setQueue(null);
                } catch (final CrawlerException e) {
                    logger.error("Crawl terminated exception : ", e);
                }
                try {
                    logger.error("Stopping crawler : " + indexableInternet.getName());
                    crawlerController.stop();
                } catch (final CrawlerException e) {
                    logger.error("Crawl terminated exception : ", e);
                }
            }
        }
        CrawlerExecutor crawlerExecutor = new CrawlerExecutor();
        IndexContext indexContext = getIndexContext(indexableInternet);
        ThreadUtilities.executeForkJoinTasks(indexContext.getName(), indexableInternet.getThreads(), crawlerExecutor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTerminated(final boolean terminated) {
        this.terminated = terminated;
    }

    /**
     * This method just walks up the parent hierarchy to find the index context.
     *
     * @param indexable the indexable to find the context for
     * @return the super parent of the indexable, i.e. the index context
     */
    private IndexContext getIndexContext(final Indexable indexable) {
        if (IndexContext.class.isAssignableFrom(indexable.getClass())) {
            return (IndexContext) indexable;
        }
        return getIndexContext(indexable.getParent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Url getResource() {
        if (isTerminated()) {
            return null;
        }
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
        // If there are no urls on the stack try the database
        if (url == null) {
            // We have to retry because sometimes there is a concurrent access
            // problem with Jpa, could change the read access perhaps to transactional?
            // Will that help?
            retry = 10;
            do {
                try {
                    logger.info("Going to database for resources : ");
                    String[] fields = {IConstants.NAME, IConstants.INDEXED};
                    Object[] values = {indexableInternet.getName(), Boolean.FALSE};
                    List<Url> dbUrls = dataBase.find(Url.class, fields, values, 0, 100);
                    // Delete the used urls from the database
                    dataBase.removeBatch(dbUrls);
                    this.urls.addAll(dbUrls);
                    break;
                } catch (final Exception e) {
                    logger.error("Exception getting and removing urls from the database, retrying : ", e);
                }
            } while (retry-- > 0);
            if (this.urls.size() > 0) {
                url = this.urls.pop();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Doing url : " + url + ", " + urls.size());
        }
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResources(final List<Url> resources) {
        if (resources == null) {
            return;
        }
        if (urls.size() + resources.size() < IConstants.ONE_THOUSAND) {
            urls.addAll(resources);
        } else {
            if (random.nextLong() % 1000 == 0) {
                // If we go over the limit for the stack size then we need to
                // persist the url in the database to avoid running out of memory
                logger.info("Persisting resources : " + resources.size() + ", urls : " + urls.size() + ", " + resources);
            }
            dataBase.persistBatch(resources);
        }
    }

    private Url getUrl(final String stringUrl, final IndexableInternet indexableInternet) {
        Url url = new Url();
        url.setIndexed(Boolean.FALSE);
        url.setName(indexableInternet.getName());
        url.setUrl(stringUrl);
        return url;
    }

}
