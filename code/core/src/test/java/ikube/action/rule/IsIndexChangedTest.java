package ikube.action.rule;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.Open;
import ikube.action.index.IndexManager;
import ikube.toolkit.FileUtilities;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 19-01-2014
 */
public class IsIndexChangedTest extends AbstractTest {

    private IndexSearcher multiSearcher;
    private IsIndexChanged isIndexChanged;

    @Before
    public void before() {
        isIndexChanged = new IsIndexChanged();
        Mockito.when(indexContext.isDelta()).thenReturn(Boolean.TRUE);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                multiSearcher = (IndexSearcher) invocation.getArguments()[0];
                return null;
            }
        }).when(indexContext).setMultiSearcher(Mockito.any(IndexSearcher.class));
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    public void execute() throws Exception {
        createIndexFileSystem(indexContext, "Hello world");

        // First open the index
        new Open().execute(indexContext);

        // Add some documents to the index and reopen
        Mockito.when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);
        IndexWriter[] indexWriters = IndexManager.openIndexWriterDelta(indexContext);

        addDocuments(indexWriters[0], IConstants.CONTENTS, "Michael Couck");
        boolean changed = isIndexChanged.evaluate(indexContext);
        assertTrue("The index should be changed : ", changed);

        // Add some more documents to the index and reopen
        addDocuments(indexWriters[0], IConstants.CONTENTS, "Michael Couck again");
        changed = isIndexChanged.evaluate(indexContext);
        assertTrue("The index should be open : ", changed);
    }

}
