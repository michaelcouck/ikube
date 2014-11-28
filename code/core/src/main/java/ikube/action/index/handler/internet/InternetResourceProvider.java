package ikube.action.index.handler.internet;

import bplatt.spider.Arachnid;
import bplatt.spider.PageInfo;
import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.THREAD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * NOTE: This class needs a crawler! It is completely broken.<br>
 * <p/>
 * Crawlers that were tried:
 * <pre>
 *     Crawler4j
 *     Flaxcrawler
 *     Arachnid
 * </pre>
 * <p/>
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
public class InternetResourceProvider extends Arachnid implements IResourceProvider<Url> {

    private static int RETRY = 3;
    private static long SLEEP = 10000;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Random random;
    private Stack<Url> urls;
    private IDataBase dataBase;
    private IndexableInternet indexableInternet;
    private boolean terminated = Boolean.FALSE;

    public InternetResourceProvider(final IndexableInternet indexableInternet, final IDataBase dataBase) throws MalformedURLException {
        super(indexableInternet.getUrl());
        initialize(indexableInternet);
        this.indexableInternet = indexableInternet;
        this.dataBase = dataBase;

        urls = new Stack<>();
        random = new Random();

        THREAD.submit(indexableInternet.getName(), new Runnable() {
            public void run() {
                InternetResourceProvider.this.traverse();
            }
        });
    }

    void initialize(final IndexableInternet indexableInternet) {
        final Pattern pattern;
        if (indexableInternet.getExcludedPattern() == null) {
            pattern = null;
        } else {
            pattern = Pattern.compile(indexableInternet.getExcludedPattern());
        }
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
    public synchronized Url getResource() {
        if (isTerminated()) {
            return null;
        }
        int retry = RETRY;
        Url url = !urls.isEmpty() ? urls.pop() : null;
        logger.error("Get resource : " + url);
        while (url == null && retry-- >= 0) {
            logger.info("Sleeping : " + urls.size());
            // ThreadUtilities.sleep(SLEEP);
            THREAD.sleep(SLEEP);
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

    @Override
    protected void handleLink(final PageInfo pageInfo) {
        try {
            logger.warn("Page info : " + pageInfo);
            pageInfo.extract(new Reader() {
                @Override
                public int read(final char[] cbuf, final int off, final int len) throws IOException {
                    logger.warn("Content : " + new String(cbuf) + ", " + off + ", " + len);
                    return 0;
                }

                @Override
                public void close() throws IOException {
                    logger.warn("close");
                }
            });
        } catch (final IOException e) {
            logger.error(null, e);
        }
    }

    @Override
    protected void handleBadLink(final URL url, final URL url2, final PageInfo pageInfo) {
        // What to do here, log it?
        logger.warn("Url : " + url + ", " + url2 + ", " + pageInfo);
    }

    @Override
    protected void handleNonHTMLlink(final URL url, final URL url2, final PageInfo pageInfo) {
        logger.warn("Url : " + url + ", " + url2 + ", " + pageInfo);
    }

    @Override
    protected void handleExternalLink(final URL url, final URL url2) {
        logger.warn("Url : " + url + ", " + url2);
    }

    @Override
    protected void handleBadIO(final URL url, final URL url2) {
        logger.warn("Url : " + url + ", " + url2);
    }
}
