package ikube.search;

import ikube.AbstractTest;
import ikube.model.IndexContext;
import ikube.toolkit.FILE;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-02-2014
 */
public class SearchToolkitTest extends AbstractTest {

    private IndexContext indexContext;

    @Before
    public void before() {
        indexContext = mock(IndexContext.class);
        File tmpFile = FILE.getFile("./target/indexes", Boolean.TRUE);
        String indexDirectoryPath = FILE.cleanFilePath(tmpFile.getAbsolutePath());
        when(indexContext.getIndexDirectoryPath()).thenReturn(indexDirectoryPath);
        when(indexContext.getName()).thenReturn("index");
        when(indexContext.getIndexName()).thenReturn("index");
        when(indexContext.getBufferedDocs()).thenReturn(10);
        when(indexContext.getBufferSize()).thenReturn(10d);
        when(indexContext.getMaxFieldLength()).thenReturn(10);
        when(indexContext.getMaxReadLength()).thenReturn(10000l);
        when(indexContext.getMergeFactor()).thenReturn(10);
        when(indexContext.getMaxAge()).thenReturn(60l);
    }

    @Test
    public void main() {
        String text = "and some data for hello world search";
        File indexDirectory = createIndexFileSystem(indexContext, System.currentTimeMillis(), "127.0.1.1", text);
        String indexDirectoryPath = FILE.cleanFilePath(indexDirectory.getAbsolutePath());
        String[] args = {indexDirectoryPath, "contents", "hello world"};
        SearchToolkit.main(args);
    }

    @Test
    @Ignore
    public void adHoc() {
        SearchToolkit.main(new String[] {
                "/mnt/sdb/home/indexes/desktop/1427401340270/192.168.1.42-8000",
                "contents",
                "couck"});
    }

}
