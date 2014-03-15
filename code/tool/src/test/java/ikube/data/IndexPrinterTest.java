package ikube.data;

import ikube.AbstractTest;
import ikube.action.index.analyzer.NgramAnalyzer;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import mockit.Deencapsulation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import java.io.File;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class IndexPrinterTest extends AbstractTest {

    private IndexContext indexContext;

    @Before
    public void before() {
        indexContext = mock(IndexContext.class);

        when(indexContext.getIndexDirectoryPath()).thenReturn("./indexes");
        when(indexContext.getIndexDirectoryPathBackup()).thenReturn("./indexes/backup");
        when(indexContext.isVectored()).thenReturn(Boolean.TRUE);
        when(indexContext.isTokenized()).thenReturn(Boolean.TRUE);
        when(indexContext.getMaxReadLength()).thenReturn(1000000l);
        when(indexContext.isOmitNorms()).thenReturn(Boolean.FALSE);
        when(indexContext.getAnalyzer()).thenReturn(new NgramAnalyzer());
        when(indexContext.getBatchSize()).thenReturn(1000);
        when(indexContext.getBufferedDocs()).thenReturn(1000);
        when(indexContext.getBufferSize()).thenReturn(256d);
        when(indexContext.getIndexName()).thenReturn("index-name");
        when(indexContext.getInternetBatchSize()).thenReturn(1000);
        when(indexContext.getMaxAge()).thenReturn(600l);
        when(indexContext.getMaxFieldLength()).thenReturn(10000);
        when(indexContext.getMaxReadLength()).thenReturn(1000000l);
        when(indexContext.getMergeFactor()).thenReturn(1000);
        when(indexContext.getNumDocsForSearchers()).thenReturn(100l);
        when(indexContext.getThrottle()).thenReturn(0l);
        when(indexContext.isCompoundFile()).thenReturn(Boolean.TRUE);
        when(indexContext.isDelta()).thenReturn(Boolean.FALSE);
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void main() {
        final String[] strings = {"and some text", "for the index", "to be able", "to search if necessary"};
        Logger logger = mock(Logger.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                for (final Object argument : invocation.getArguments()) {
                    for (int i = 0; i < strings.length; i++) {
                        if (strings[i] != null && argument.toString().contains(strings[i])) {
                            strings[i] = null;
                        }
                    }
                }
                return null;
            }
        }).when(logger).info(any(String.class));
        Deencapsulation.setField(IndexPrinter.class, "LOGGER", logger);

        File file = createIndexFileSystem(indexContext, System.currentTimeMillis(), "127.0.0.1", strings);
        String[] args = {file.getAbsolutePath(), "100"};
        IndexPrinter.main(args);
        for (final String string : strings) {
            assertNull(string);
        }
    }

}
