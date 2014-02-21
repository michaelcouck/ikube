package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.handler.IResourceProvider;
import ikube.database.IDataBase;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;
import org.niocchi.core.*;
import org.niocchi.gc.GenericResource;
import org.niocchi.gc.GenericResourceFactory;
import org.niocchi.gc.GenericWorker;
import org.niocchi.urlpools.TimeoutURLPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Michael Couck
 * @version 02.00
 * @since 21-06-2013
 */
public class InternetResourceProvider implements IResourceProvider<Url>, URLPool {

    public class WorkerImpl extends GenericWorker {

        public WorkerImpl(final Crawler crawler) {
            super(crawler, indexableInternet.getName());
        }

        @Override
        public void processResource(final Query query) {
            GenericResource genericResource = (GenericResource) query.getResource();
            String stringUrl = query.getOriginalURL().toExternalForm();
            String contentType = query.getResource().getContentType();
            String filePath = FileUtilities.cleanFilePath(genericResource.getTmpFileAbsolutePath());
            File file = new File(filePath);

            byte[] rawContent = FileUtilities.getContents(file, indexableInternet.getMaxReadLength()).toByteArray();
            FileUtilities.deleteFile(file);
            logger.debug("Setting content length : " + rawContent.length);
            Url url = findUrl(stringUrl);
            url.setContentType(contentType);
            url.setRawContent(rawContent);
            urls.add(url);
        }
    }

    private static int RETRY = 5;
    private static long SLEEP = 6000;
    private static final String[] FIELDS = new String[]{IConstants.NAME, IConstants.URL};

    private Logger logger = LoggerFactory.getLogger(InternetResourceProvider.class);
    private TreeSet<Url> urls;
    private IndexableInternet indexableInternet;
    private IDataBase dataBase;

    public InternetResourceProvider(final IndexableInternet indexableInternet, final IDataBase dataBase) {
        this.indexableInternet = indexableInternet;
        this.dataBase = dataBase;
        initialize(indexableInternet);
    }

    void initialize(final IndexableInternet indexableInternet) {
        try {
            urls = new TreeSet<>(new Comparator<Url>() {
                @Override
                public int compare(final Url o1, final Url o2) {
                    return o1.getUrl().compareTo(o2.getUrl());
                }
            });

            final Crawler crawler = new Crawler(new GenericResourceFactory(), 100);
            crawler.setUserAgent("firefox 3.0");
            crawler.setTimeout(indexableInternet.getTimeout());
            crawler.setAllowCompression(Boolean.FALSE);
            // crawler.setVerbose();

            Url seedUrl = getUrl(indexableInternet.getUrl());
            dataBase.persist(seedUrl);

            final URLPool urlPool = new TimeoutURLPool(this);
            for (int i = 0; i < indexableInternet.getThreads(); i++) {
                Worker worker = new WorkerImpl(crawler);
                ThreadUtilities.submit(indexableInternet.getParent().getName(), worker);
            }
            ThreadUtilities.submit(indexableInternet.getParent().getName(), new Runnable() {
                public void run() {
                    try {
                        crawler.run(urlPool);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Url getUrl(final String stringUrl) {
        Url url = new Url();
        url.setIndexed(Boolean.FALSE);
        url.setName(indexableInternet.getName());
        url.setUrl(stringUrl);
        return url;
    }


    private Url findUrl(final String stringUrl) {
        Object[] values = new Object[]{indexableInternet.getName(), stringUrl};
        return dataBase.find(Url.class, FIELDS, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Url getResource() {
        int retry = RETRY;
        while (urls.isEmpty() && retry-- >= 0) {
            try {
                wait(SLEEP);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        if (urls.isEmpty()) {
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Urls size : " + urls.size() + ", " + urls.isEmpty());
        }
        Url url = urls.first();
        logger.debug("Popping : " + url);
        urls.remove(url);
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
        for (final Url url : resources) {
            Url dbUrl = findUrl(url.getUrl());
            if (dbUrl != null) {
                continue;
            }
            logger.debug("Persisting : " + url);
            dataBase.persist(url);
        }
    }

    @Override
    public boolean hasNextQuery() {
        Url resource = waitForUrl();
        boolean hasNext = resource != null;
        logger.debug("Has next : " + hasNext);
        return hasNext;
    }

    @Override
    public Query getNextQuery() throws URLPoolException {
        Url url = waitForUrl();
        Query query = null;
        if (url != null) {
            String absUrl = UriUtilities.stripAnchor(url.getUrl(), "");
            url.setIndexed(Boolean.TRUE);
            dataBase.merge(url);
            try {
                query = new Query(absUrl);
            } catch (final MalformedURLException e) {
                logger.error("Mal formed url : " + url, e);
            }
        }
        logger.debug("Next query : " + query);
        return query;
    }

    @Override
    public void setProcessed(final Query query) {
        logger.debug("Processed : " + query);
    }

    private Url waitForUrl() {
        int retry = RETRY;
        String[] fields = new String[]{IConstants.NAME, IConstants.INDEXED};
        Object[] values = new Object[]{indexableInternet.getName(), Boolean.FALSE};
        Url url = dataBase.find(Url.class, fields, values);
        while (url == null && retry-- > 0) {
            // dbStats();
            logger.debug("Url null, sleeping for a while : ");
            ThreadUtilities.sleep(SLEEP);
            url = dataBase.find(Url.class, fields, values);
        }
        logger.debug("Got url : " + url);
        return url;
    }

    @SuppressWarnings("UnusedDeclaration")
    private void dbStats() {
        long count = dataBase.count(Url.class);
        logger.info("Urls : " + count);
        List<Url> dbUrls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
        for (final Url dbUrl : dbUrls) {
            logger.info("Url : " + dbUrl);
        }
    }

}

// Monitor monitor = new Monitor(8100);
// monitor.addMonitored(new MonitorImpl());
// monitor.start();

/*
public class MonitorImpl implements Monitorable {

    @Override
    public void printMonitoredState(final PrintStream printStream) {
    }

    @Override
    public void dump() {
    }
}*/
