package ikube.action;

import ikube.AbstractTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static ikube.action.SynchronizeCallable.FileChunk;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-04-2014
 */
@Ignore
public class SynchronizeCallableTest extends AbstractTest {

    private FileChunk fileChunk;
    private SynchronizeCallable synchronizeCallable;

    @Before
    public void before() {
        fileChunk = new FileChunk();
        synchronizeCallable = new SynchronizeCallable(fileChunk, indexContext);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void call() throws Exception {
        File indexDirectory = createIndexFileSystem(indexContext, System.currentTimeMillis(), ip, "create an", "index with", "something in it");
        File[] indexFiles = indexDirectory.listFiles();
        for (final File indexFile : indexFiles) {
            int transferred = 0;
            long length = indexFile.length();
            fileChunk.name = indexFile.getName();
            fileChunk.length = 1024 * 1024;
            while (transferred < length) {
                fileChunk = synchronizeCallable.call();
                transferred += fileChunk.length;
                fileChunk.offset = transferred;
                logger.info("Transferred : " + transferred + ", read : " + fileChunk.length);
            }
        }
        synchronizeCallable.call();
    }

}
