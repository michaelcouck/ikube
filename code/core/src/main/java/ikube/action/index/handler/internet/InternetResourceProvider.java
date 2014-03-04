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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

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

            Url url = findUrl(indexableInternet.getName(), stringUrl);
            url.setContentType(contentType);
            if (file.exists() && file.canRead()) {
                ByteArrayOutputStream byteArrayOutputStream = FileUtilities.getContents(file, indexableInternet.getMaxReadLength());
                if (byteArrayOutputStream != null) {
                    byte[] rawContent = byteArrayOutputStream.toByteArray();
                    logger.debug("Setting content length : " + rawContent.length);
                    if (rawContent != null && rawContent.length > 0) {
                        url.setRawContent(rawContent);
                    }
                }
            }
            FileUtilities.deleteFile(file);
            urls.add(url);
        }
    }

    private static int RETRY = 5;
    private static long SLEEP = 6000;
    private static final String[] FIELDS = new String[]{IConstants.NAME, IConstants.URL};

    private Logger logger = LoggerFactory.getLogger(InternetResourceProvider.class);

    private TreeSet<Url> urls;
    private IDataBase dataBase;
    private IndexableInternet indexableInternet;

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
            setResources(Arrays.asList(seedUrl));

            final URLPool urlPool = new TimeoutURLPool(this);
            for (int i = 0; i < indexableInternet.getThreads(); i++) {
                Worker worker = new WorkerImpl(crawler);
                ThreadUtilities.submit(indexableInternet.getName(), worker);
            }
            ThreadUtilities.submit(indexableInternet.getName(), new Runnable() {
                public void run() {
                    try {
                        logger.info("Starting crawl : " + indexableInternet.getName());
                        crawler.run(urlPool);
                        logger.info("Finishing crawl : " + indexableInternet.getName());
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        ThreadUtilities.destroy(indexableInternet.getName());
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


    private Url findUrl(final String name, final String stringUrl) {
        Object[] values = new Object[]{name, stringUrl};
        return dataBase.find(Url.class, FIELDS, values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Url getResource() {
        int retry = RETRY;
        Url url = urls.pollFirst();
        while (url == null && retry-- >= 0) {
            ThreadUtilities.sleep(SLEEP);
            url = urls.pollFirst();
        }
        boolean contains = urls.contains(url);
        logger.debug("Doing url : " + url + ", " + contains + ", " + urls.size());
        if (url != null && contains) {
            urls.remove(url);
        }
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setResources(final List<Url> resources) {
        if (resources == null) {
            return;
        }
        for (final Url url : resources) {
            Url dbUrl = findUrl(indexableInternet.getName(), url.getUrl());
            if (dbUrl != null) {
                logger.debug("Not persisting : " + url);
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
            logger.debug("No url in database, sleeping for a while : ");
            ThreadUtilities.sleep(SLEEP);
            url = dataBase.find(Url.class, fields, values);
        }
        logger.debug("Got url : " + url);
        return url;
    }

    @SuppressWarnings("UnusedDeclaration")
    private void dbStats() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(IConstants.NAME, indexableInternet.getName());
        parameters.put(IConstants.INDEXED, Boolean.FALSE);

        long count = dataBase.count(Url.class);
        long notIndexed = dataBase.count(Url.class, parameters);
        logger.info("Urls in database : " + count + ", not indexed : " + notIndexed);
        List<Url> dbUrls = dataBase.find(Url.class, 0, Integer.MAX_VALUE);
        for (final Url dbUrl : dbUrls) {
            logger.info("        : url : " + dbUrl.getUrl());
        }
    }

}