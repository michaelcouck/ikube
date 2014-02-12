package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.analytics.IAnalyzer;
import ikube.analytics.toolkit.Correlation;
import ikube.model.Context;
import ikube.model.IndexableTweets;
import ikube.toolkit.ObjectToolkit;
import ikube.toolkit.PerformanceTester;
import ikube.toolkit.ThreadUtilities;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.springframework.social.twitter.api.Tweet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05.12.13
 */
public class ClassifierTrainingStrategyTest extends AbstractTest {

    @SuppressWarnings("rawtypes")
    private IAnalyzer analyzer;
    private ReentrantLock reentrantLock;
    /**
     * Class under test.
     */
    private ClassifierTrainingStrategy classifierTrainingStrategy;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        analyzer = mock(IAnalyzer.class);
        reentrantLock = new ReentrantLock(Boolean.TRUE);
        Context context = mock(Context.class);
        when(context.getAnalyzer()).thenReturn(analyzer);
        when(context.getMaxTraining()).thenReturn(100);

        classifierTrainingStrategy = new ClassifierTrainingStrategy();
        classifierTrainingStrategy.setContext(context);
        classifierTrainingStrategy.initialize();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aroundProcess() throws Exception {
        Document document = new Document();
        final IndexableTweets indexableTweets = mock(IndexableTweets.class);
        when(indexableTweets.isStored()).thenReturn(Boolean.TRUE);
        when(indexableTweets.isAnalyzed()).thenReturn(Boolean.TRUE);
        when(indexableTweets.getContent()).thenReturn(IConstants.CONTENT);
        when(analyzer.sizeForClassOrCluster(any())).thenReturn(1);

        IndexManager.addStringField(IConstants.LANGUAGE, Locale.ENGLISH.getLanguage(), indexableTweets, document);
        IndexManager.addStringField(IConstants.CLASSIFICATION, IConstants.POSITIVE, indexableTweets, document);

        final Tweet tweet = (Tweet) ObjectToolkit.getObject(Tweet.class);
        ObjectToolkit.populateFields(tweet, Boolean.TRUE, 10);

        classifierTrainingStrategy.setLanguage(Locale.ENGLISH.getLanguage());
        classifierTrainingStrategy.aroundProcess(indexContext, indexableTweets, document, tweet);
        verify(analyzer).train(any(Object[].class));

        int iterations = 11;
        PerformanceTester.execute(new PerformanceTester.APerform() {
            @Override
            public void execute() {
                try {
                    classifierTrainingStrategy.aroundProcess(indexContext, indexableTweets, new Document(), tweet);
                } catch (final Exception e) {
                    logger.error(null, e);
                }
            }
        }, "Classifier training performance : ", iterations, Boolean.TRUE);

        synchronized (this) {
            logger.info("This : " + this + ", " + Thread.currentThread());
        }
    }


    @Test
    public void performance() {
        ThreadUtilities.initialize();
        final int iterations = 10000;
        final AtomicLong syncAtomicLong = new AtomicLong(10000);
        final AtomicLong blockAtomicLong = new AtomicLong(10000);
        final AtomicLong lockAtomicLong = new AtomicLong(10000);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Future<?> future = ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
                public void run() {
                    double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
                        public void execute() throws Throwable {
                            syncPerformance();
                        }
                    }, "Synchronized method : ", iterations, Boolean.FALSE);
                    if (executionsPerSecond < syncAtomicLong.get()) {
                        syncAtomicLong.set((long) executionsPerSecond);
                    }
                }
            });
            futures.add(future);
        }
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
        futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Future<?> future = ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
                public void run() {
                    double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
                        public void execute() throws Throwable {
                            syncBlockPerformance();
                        }
                    }, "Synchronized block : ", iterations, Boolean.FALSE);
                    if (executionsPerSecond < blockAtomicLong.get()) {
                        blockAtomicLong.set((long) executionsPerSecond);
                    }
                }
            });
            futures.add(future);
        }
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
        futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Future<?> future = ThreadUtilities.submit(this.getClass().getSimpleName(), new Runnable() {
                public void run() {
                    double executionsPerSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
                        public void execute() throws Throwable {
                            syncLockPerformance();
                        }
                    }, "Synchronized lock : ", iterations, Boolean.FALSE);
                    if (executionsPerSecond < lockAtomicLong.get()) {
                        lockAtomicLong.set((long) executionsPerSecond);
                    }
                }
            });
            futures.add(future);
        }
        ThreadUtilities.waitForFutures(futures, Integer.MAX_VALUE);
        logger.info("Sync : " + syncAtomicLong.get() + ", block : " + blockAtomicLong.get() + ", lock : " + lockAtomicLong.get());
    }

    private synchronized void syncPerformance() {
        new Correlation().correlate();
    }

    private void syncBlockPerformance() {
        synchronized (this) {
            new Correlation().correlate();
        }
    }

    private void syncLockPerformance() {
        reentrantLock.lock();
        new Correlation().correlate();
        reentrantLock.unlock();
    }
}