package ikube.action;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Server;
import ikube.model.Snapshot;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.OsUtilities;
import mockit.Deencapsulation;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-04-2014
 */
public class SynchronizeTest extends AbstractTest {

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void internalExecute() throws Exception {
        Future<String[]> indexFilesFuture = getIndexFilesFuture();
        Future<byte[]> chunkFuture = getChunkFuture();

        Map<String, Server> servers = getServers();

        when(clusterManager.getServer()).thenReturn(servers.get(IConstants.ADDRESS));
        when(clusterManager.getServers()).thenReturn(servers);
        when(clusterManager.sendTaskTo(any(Server.class), any(Callable.class))).thenReturn(indexFilesFuture, chunkFuture);

        Synchronize synchronize = new Synchronize();
        Deencapsulation.setField(synchronize, "clusterManager", clusterManager);
        synchronize.execute(indexContext);

        try {
            // There are five chunks and four extra files, so nine calls
            verify(chunkFuture, times(9)).get();
        } catch (final Throwable e) {
            logger.error("Exception doing the synchronize test on os : ", e);
            if (OsUtilities.isOs("3.11.0-12-generic")) {
                throw e;
            }
        }
    }

    private Map<String, Server> getServers() {
        Server serverOne = getServer();
        Server serverTwo = getServer();
        Server serverThree = getServer();

        servers.put(IConstants.ADDRESS, serverOne);
        servers.put("192.168.1.20", serverTwo);
        servers.put("192.168.1.30", serverThree);

        return servers;
    }

    private Server getServer() {
        Server server = mock(Server.class);
        Snapshot snapshot = mock(Snapshot.class);
        when(snapshot.getLatestIndexTimestamp()).thenReturn(new Date());
        when(server.getIndexContexts()).thenReturn(Arrays.asList(indexContext));
        when(server.getAddress()).thenReturn(Long.toString(System.currentTimeMillis()));
        when(indexContext.getSnapshot()).thenReturn(snapshot);
        return server;
    }

    @SuppressWarnings("unchecked")
    private Future<byte[]> getChunkFuture() throws Exception {
        byte[] chunk = new byte[1024];
        Future<byte[]> chunkFuture = mock(Future.class);
        when(chunkFuture.get()).thenReturn(chunk, chunk, chunk, chunk, new byte[0]);
        return chunkFuture;
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private Future<String[]> getIndexFilesFuture() throws Exception {
        String[] strings = {"create an", "index with", "something in it"};
        File indexDirectory = createIndexFileSystem(indexContext, strings);
        File[] files = indexDirectory.listFiles();
        String[] indexFiles = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            indexFiles[i] = files[i].getAbsolutePath();
        }
        Future<String[]> future = mock(Future.class);
        when(future.get()).thenReturn(indexFiles);
        return future;
    }

}
