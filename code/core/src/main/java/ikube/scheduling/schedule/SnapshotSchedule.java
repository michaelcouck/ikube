package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.*;
import ikube.scheduling.Schedule;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Timestamp;
import java.util.*;

import static ikube.action.index.IndexManager.*;

/**
 * This schedule will take a snapshot of various system states periodically, including the cpu, how many searches
 * there have been on all the indexes etc. Snapshots are then persisted to the database, and cleaned from time to time
 * in the {@link ikube.action.Reset} action so the database doesn't fill up. Typically we only need a few snapshots
 * and not two weeks worth. This schedule should run every minute.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 22-07-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SnapshotSchedule extends Schedule {

    static final int MAX_SNAPSHOTS_CONTEXT = 90;
    static final double ONE_MINUTE_MILLIS = 60000;

    @Autowired
    private IDataBase dataBase;
    @Autowired
    private IMonitorService monitorService;
    @Autowired
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void run() {
        Server server = clusterManager.getServer();
        setServerStatistics(server);
        Map<String, IndexContext> indexContexts = monitorService.getIndexContexts();
        for (final Map.Entry<String, IndexContext> mapEntry : indexContexts.entrySet()) {
            try {
                IndexContext indexContext = mapEntry.getValue();
                indexContext.setNumDocsForSearchers(getNumDocsForIndexSearchers(indexContext));
                indexContext.setIndexing(indexContext.getIndexWriters() != null && indexContext.getIndexWriters().length > 0);

                Snapshot snapshot = new Snapshot();

                snapshot.setAvailableProcessors(server.getProcessors());
                snapshot.setSystemLoad(server.getAverageCpuLoad());

                snapshot.setIndexContext(indexContext.getName());
                snapshot.setTimestamp(new Timestamp(System.currentTimeMillis()));

                snapshot.setNumDocsForIndexWriters(getNumDocsForIndexWriters(indexContext));
                snapshot.setIndexSize(getIndexSize(indexContext));
                snapshot.setLatestIndexTimestamp(getLatestIndexDirectoryDate(indexContext));

                snapshot.setDocsPerMinute(getDocsPerMinute(indexContext, snapshot));
                snapshot.setTotalSearches(getTotalSearchesForIndex(indexContext).longValue());
                snapshot.setSearchesPerMinute(getSearchesPerMinute(indexContext, snapshot));

                if (indexContext.getNumDocsForSearchers() == 0) {
                    indexContext.setNumDocsForSearchers(snapshot.getNumDocsForIndexWriters());
                }

                dataBase.persist(snapshot);
                String[] names = new String[]{IConstants.INDEX_CONTEXT};
                Object[] values = new Object[]{indexContext.getName()};
                List<Snapshot> snapshots = dataBase.find(
                        Snapshot.class,
                        Snapshot.SELECT_SNAPSHOTS_ORDER_BY_TIMESTAMP_DESC,
                        names,
                        values,
                        0,
                        MAX_SNAPSHOTS_CONTEXT);
                List<Snapshot> sortedSnapshots = sortSnapshots(snapshots);
                indexContext.setSnapshots(sortedSnapshots);

                // Find the last snapshot and put it in the action if there is one executing on the index context
                for (final Action action : server.getActions()) {
                    if (action.getIndexName().equals(indexContext.getIndexName())) {
                        action.setSnapshot(snapshot);
                        break;
                    }
                }
            } catch (final Exception e) {
                logger.error("Exception persisting snapshot : ", e);
            }
        }
    }

    protected List<Snapshot> sortSnapshots(final List<Snapshot> snapshots) {
        List<Snapshot> sortedSnapshots = new ArrayList<>(snapshots);
        // We have the last snapshots, now reverse the order for the gui
        Comparator<Snapshot> comparator = new Comparator<Snapshot>() {
            @Override
            public int compare(Snapshot o1, Snapshot o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        };
        Collections.sort(sortedSnapshots, comparator);
        return sortedSnapshots;
    }

    protected void setServerStatistics(final Server server) {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        server.setArchitecture(operatingSystemMXBean.getArch());
        server.setProcessors(operatingSystemMXBean.getAvailableProcessors());
        server.setAverageCpuLoad(operatingSystemMXBean.getSystemLoadAverage());
        server.setFreeMemory(Runtime.getRuntime().freeMemory() / IConstants.MILLION);
        server.setMaxMemory(Runtime.getRuntime().maxMemory() / IConstants.MILLION);
        server.setTotalMemory(Runtime.getRuntime().totalMemory() / IConstants.MILLION);
        server.setThreadsRunning(ThreadUtilities.isInitialized());
        server.setAge(System.currentTimeMillis());
        server.setTimestamp(new Timestamp(System.currentTimeMillis()));
        /*try {
            // @Michael on 27.09.13: If this is executed too many times on a raid 10 disk the disk crashes!!!
            long availableDiskSpace = FileSystemUtils.freeSpaceKb("/") / IConstants.MILLION;
            server.setFreeDiskSpace(availableDiskSpace);
        } catch (Exception e) {
            logger.error("Exception accessing the disk space : ", e);
        }*/
        // logger.info("Threads running : " + server.isThreadsRunning() + ", " + ThreadUtilities.isInitialized());
        setLogTail(server);
    }

    void setLogTail(final Server server) {
        File logFile = Logging.getLogFile();
        if (logFile == null || !logFile.exists() || !logFile.isFile() || !logFile.canRead()) {
            String message = "Can't find log file : " + logFile;
            logger.warn(message);
            return;
        }
        RandomAccessFile inputStream = null;
        try {
            inputStream = new RandomAccessFile(logFile, "r");
            int fileLength = (int) logFile.length();
            int offset = Math.max(fileLength - (IConstants.MILLION / 100), 0);
            int lengthToRead = Math.max(0, fileLength - offset);
            byte[] bytes = new byte[lengthToRead];
            inputStream.seek(offset);
            inputStream.read(bytes, 0, lengthToRead);
            server.setLogTail(new String(bytes));
        } catch (final FileNotFoundException e) {
            logger.error("Log file not found : " + e.getMessage());
        } catch (final Exception e) {
            logger.error("Error reading log file : ", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @SuppressWarnings("rawtypes")
    protected Number getTotalSearchesForIndex(final IndexContext indexContext) {
        String[] fields = {"indexName"};
        Object[] values = {indexContext.getIndexName()};
        long totalSearches = dataBase.execute(Search.SELECT_FROM_SEARCH_COUNT_SEARCHES, fields, values);
        logger.debug("Total search database : " + indexContext.getIndexName() + ", " + totalSearches);
        return totalSearches;
    }

    protected long getSearchesPerMinute(final IndexContext indexContext, final Snapshot snapshot) {
        List<Snapshot> snapshots = indexContext.getSnapshots();
        if (snapshots == null || snapshots.size() < 1) {
            return 0;
        }
        Snapshot previous = snapshots.get(snapshots.size() - 1);
        long searchesPerMinute = snapshot.getTotalSearches() - previous.getTotalSearches();
        searchesPerMinute = Math.max(0, searchesPerMinute);
        logger.debug("Previous : " + previous.getTotalSearches() +
                ", total : " + snapshot.getTotalSearches() +
                ", per minute : " + searchesPerMinute);
        return searchesPerMinute;
    }

    protected long getDocsPerMinute(final IndexContext indexContext, final Snapshot current) {
        List<Snapshot> snapshots = indexContext.getSnapshots();
        if (snapshots == null || snapshots.size() == 0) {
            return 0;
        }
        normalizeDocsPerMinute(indexContext);
        normalizeNumDocsForIndexWriters(indexContext);
        Snapshot previous = snapshots.get(snapshots.size() - 1);
        double ratio = getRatio(previous, current);
        long docsPerMinute = (long) ((current.getNumDocsForIndexWriters() - previous.getNumDocsForIndexWriters()) / ratio);
        if (docsPerMinute < 0) {
            docsPerMinute = Math.abs(docsPerMinute);
        }
        if (docsPerMinute > IConstants.MILLION) {
            docsPerMinute = previous.getDocsPerMinute();
        }
        return docsPerMinute;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void normalizeDocsPerMinute(final IndexContext indexContext) {
        List<Snapshot> snapshots = indexContext.getSnapshots();
        if (snapshots.size() < 3) {
            return;
        }
        for (int i = 0; i < snapshots.size() - 3; i++) {
            Snapshot previous = snapshots.get(i);
            Snapshot current = snapshots.get(i + 1);
            Snapshot next = snapshots.get(i + 2);

            if (previous.getDocsPerMinute() < 0) {
                previous.setDocsPerMinute(0);
                dataBase.merge(previous);
            }

            long previousDocsPerMinute = previous.getDocsPerMinute();
            long currentDocsPerMinute = current.getDocsPerMinute();
            long nextDocsPerMinute = next.getDocsPerMinute();

            if (currentDocsPerMinute > (previousDocsPerMinute * 5)) {
                if (currentDocsPerMinute > (nextDocsPerMinute * 5)) {
                    long docsPerMinute = Math.abs((previousDocsPerMinute + nextDocsPerMinute) / 2l);
                    current.setDocsPerMinute(docsPerMinute);
                    dataBase.merge(current);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void normalizeNumDocsForIndexWriters(final IndexContext indexContext) {
        List<Snapshot> snapshots = indexContext.getSnapshots();
        if (snapshots.size() < 3) {
            return;
        }
        for (int i = 0; i < snapshots.size() - 3; i++) {
            Snapshot previous = snapshots.get(i);
            Snapshot current = snapshots.get(i + 1);
            Snapshot next = snapshots.get(i + 2);

            if (previous.getNumDocsForIndexWriters() < 0) {
                previous.setNumDocsForIndexWriters(0);
                dataBase.merge(previous);
            }

            long previousNumDocs = previous.getNumDocsForIndexWriters();
            long currentNumDocs = current.getNumDocsForIndexWriters();
            long nextNumDocs = next.getNumDocsForIndexWriters();

            if (currentNumDocs > (previousNumDocs * 5)) {
                if (currentNumDocs > (nextNumDocs * 5)) {
                    long numDocsForIndexWriters = Math.abs((previousNumDocs + nextNumDocs) / 2l);
                    current.setNumDocsForIndexWriters(numDocsForIndexWriters);
                    dataBase.merge(current);
                }
            }
        }
    }

    protected double getRatio(final Snapshot previous, final Snapshot snapshot) {
        double interval = snapshot.getTimestamp().getTime() - previous.getTimestamp().getTime();
        return interval / ONE_MINUTE_MILLIS;
    }

}