package ikube.analytics;

import ikube.IConstants;
import ikube.analytics.action.*;
import ikube.cluster.IClusterManager;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.CSV;
import ikube.toolkit.THREAD;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static ikube.toolkit.FILE.*;

/**
 * This class is implemented as a state pattern. The user specifies the type of analyzer, and the
 * service 'connects' to the correct implementation and executes the analysis logic.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class AnalyticsService<I, O> implements IAnalyticsService<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    private Map<String, Context> contexts;
    @Autowired
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context create(final Context context) {
        Create creator = new Create(context);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(creator);
        LOGGER.info("Calling remote members in cluster with task : " + futures.size() + ", " + creator);
        THREAD.waitForFutures(futures, IConstants.ANALYTICS_CLUSTER_WAIT_TIME);
        LOGGER.debug("Contexts : " + getContexts());
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean upload(final String fileName, final InputStream inputStream) {
        File outputFile = getOrCreateFile(new File(IConstants.ANALYTICS_DIRECTORY, fileName));
        setContents(outputFile, inputStream);
        LOGGER.debug("Upload details : " + fileName + "," + inputStream);
        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[][][] data(final Context context, final int rows) {
        String[] fileNames = getContext(context.getName()).getFileNames();
        Object[][][] prunedMatrices = new Object[fileNames.length][][];
        for (int j = 0; j < fileNames.length; j++) {
            String fileName = fileNames[j];
            File file = findFileRecursively(new File(IConstants.ANALYTICS_DIRECTORY), fileName);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                Object[][] matrix = CSV.getCsvData(fileInputStream);
                int maxRows = Math.min(matrix.length, rows);
                Object[][] prunedMatrix = new Object[maxRows][];
                System.arraycopy(matrix, 0, prunedMatrix, 0, maxRows);
                prunedMatrices[j] = prunedMatrix;
            } catch (final FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return prunedMatrices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context train(final Analysis<I, O> analysis) {
        Train trainer = new Train(analysis);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(trainer);
        LOGGER.info("Calling remote members in cluster with task : " + futures.size() + ", " + trainer);
        THREAD.waitForFutures(futures, IConstants.ANALYTICS_CLUSTER_WAIT_TIME);
        LOGGER.debug("Contexts : " + getContexts());
        return getContext(analysis.getContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Context build(final Analysis<I, O> analysis) {
        Build builder = new Build(analysis);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(builder);
        LOGGER.info("Calling remote members in cluster with task : " + futures.size() + ", " + builder);
        THREAD.waitForFutures(futures, IConstants.ANALYTICS_CLUSTER_WAIT_TIME);
        LOGGER.debug("Contexts : " + getContexts());
        return getContext(analysis.getContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Analysis<I, O> analyze(final Analysis<I, O> analysis) {
        Analyze analyzer = new Analyze(analysis);
        if (analysis.isDistributed()) {
            // Set the flag so we don't get infinite recursion
            analysis.setDistributed(Boolean.FALSE);
            // Create the callable that will be executed on one of the nodes
            Future<?> future = clusterManager.sendTask(analyzer);
            try {
                LOGGER.info("Calling remote members in cluster with task : " + analyzer);
                return (Analysis<I, O>) future.get(60, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return analyzer.call();
            } catch (final Exception e) {
                throw new RuntimeException("Exception processing analysis : " + ToStringBuilder.reflectionToString(analysis), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Analysis<I, O> sizesForClassesOrClusters(final Analysis<I, O> analysis) {
        // Create the callable that will be executed on the remote node
        SizesForClassesOrClusters sizesForClassesOrClusters = new SizesForClassesOrClusters(analysis);
        if (analysis.isDistributed()) {
            // Set the flag so we don't get infinite recursion
            analysis.setDistributed(Boolean.FALSE);
            Future<?> future = clusterManager.sendTask(sizesForClassesOrClusters);
            try {
                LOGGER.info("Calling remote members in cluster with task : " + sizesForClassesOrClusters);
                return (Analysis<I, O>) future.get(60, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return sizesForClassesOrClusters.call();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void destroy(final Context context) {
        // Create the callable that will be executed on the remote node
        Destroy destroyer = new Destroy(context);
        List<Future<Boolean>> futures = clusterManager.sendTaskToAll(destroyer);
        LOGGER.info("Calling remote members in cluster with task : " + futures.size() + ", " + destroyer);
        THREAD.waitForFutures(futures, IConstants.ANALYTICS_CLUSTER_WAIT_TIME);
        LOGGER.debug("Contexts : " + getContexts());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getContext(final String name) {
        LOGGER.debug("Contexts : " + getContexts());
        return getContexts().get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Context> getContexts() {
        LOGGER.debug("Contexts : " + contexts);
        return contexts;
    }

}