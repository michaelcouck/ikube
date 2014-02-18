package edu.uci.ics.crawler4j;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import ikube.action.index.handler.internet.InternetWebCrawler;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * @author Michael Couck
 * @version 02.00
 * @since 18-02-2014
 */
public class CrawlControllerExt extends CrawlController {

    static final Logger logger = Logger.getLogger(CrawlControllerExt.class.getName());

    Pattern pattern;
    Stack<Url> urls;
    IndexableInternet indexableInternet;

    public CrawlControllerExt(
            final CrawlConfig config,
            final PageFetcher pageFetcher,
            final RobotstxtServer robotstxtServer,
            final IndexableInternet indexableInternet,
            final Pattern pattern,
            final Stack<Url> urls)
            throws Exception {
        super(config, pageFetcher, robotstxtServer);
        this.indexableInternet = indexableInternet;
        this.pattern = pattern;
        this.urls = urls;
    }

    @SuppressWarnings("unchecked")
    protected <T extends WebCrawler> void start(final Class<T> _c, final int numberOfCrawlers, boolean isBlocking) {
        try {
            finished = false;
            crawlersLocalData.clear();
            final List<Thread> threads = new ArrayList<>();
            final List<T> crawlers = new ArrayList<>();

            for (int i = 1; i <= numberOfCrawlers; i++) {
                T crawler = (T) new InternetWebCrawler(indexableInternet, pattern, urls);
                Thread thread = new Thread(crawler, "Crawler " + i);
                crawler.setThread(thread);
                crawler.init(i, this);
                thread.start();
                crawlers.add(crawler);
                threads.add(thread);
                logger.info("Crawler " + i + " started.");
            }

            final CrawlController controller = this;

            Thread monitorThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        synchronized (waitingLock) {

                            while (true) {
                                sleep(10);
                                boolean someoneIsWorking = false;
                                for (int i = 0; i < threads.size(); i++) {
                                    Thread thread = threads.get(i);
                                    if (!thread.isAlive()) {
                                        if (!shuttingDown) {
                                            logger.info("Thread " + i + " was dead, I'll recreate it.");
                                            T crawler = _c.newInstance();
                                            thread = new Thread(crawler, "Crawler " + (i + 1));
                                            threads.remove(i);
                                            threads.add(i, thread);
                                            crawler.setThread(thread);
                                            crawler.init(i + 1, controller);
                                            thread.start();
                                            crawlers.remove(i);
                                            crawlers.add(i, crawler);
                                        }
                                    } else if (crawlers.get(i).isNotWaitingForNewURLs()) {
                                        someoneIsWorking = true;
                                    }
                                }
                                if (!someoneIsWorking) {
                                    // Make sure again that none of the threads are alive.
                                    logger.info("It looks like no thread is working, waiting for 10 seconds to make sure...");
                                    sleep(10);

                                    someoneIsWorking = false;
                                    for (int i = 0; i < threads.size(); i++) {
                                        Thread thread = threads.get(i);
                                        if (thread.isAlive() && crawlers.get(i).isNotWaitingForNewURLs()) {
                                            someoneIsWorking = true;
                                        }
                                    }
                                    if (!someoneIsWorking) {
                                        if (!shuttingDown) {
                                            long queueLength = frontier.getQueueLength();
                                            if (queueLength > 0) {
                                                continue;
                                            }
                                            logger.info("No thread is working and no more URLs are in queue waiting for another 10 seconds to make sure...");
                                            sleep(10);
                                            queueLength = frontier.getQueueLength();
                                            if (queueLength > 0) {
                                                continue;
                                            }
                                        }

                                        logger.info("All of the crawlers are stopped. Finishing the process...");
                                        // At this step, frontier notifies the threads that were
                                        // waiting for new URLs and they should stop
                                        frontier.finish();
                                        for (T crawler : crawlers) {
                                            crawler.onBeforeExit();
                                            crawlersLocalData.add(crawler.getMyLocalData());
                                        }

                                        logger.info("Waiting for 10 seconds before final clean up...");
                                        sleep(10);

                                        frontier.close();
                                        docIdServer.close();
                                        pageFetcher.shutDown();

                                        finished = true;
                                        waitingLock.notifyAll();

                                        return;
                                    }
                                }
                            }
                        }
                    } catch (final Exception e) {
                        logger.error(null, e);
                    }
                }
            });

            monitorThread.start();

            if (isBlocking) {
                waitUntilFinish();
            }

        } catch (final Exception e) {
            logger.error(null, e);
        }
    }
}
