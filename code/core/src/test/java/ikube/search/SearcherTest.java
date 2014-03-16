package ikube.search;

import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Search;
import ikube.toolkit.ApplicationContextManager;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class SearcherTest extends AbstractTest {

    private Search search;
    private Searcher searcher;

    @Before
    public void before() {
        search = mock(Search.class);
        searcher = new Searcher(search);
        Mockit.setUpMocks(ApplicationContextManagerMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManager.class);
    }

    @Test
    public void call() throws Exception {
        ISearcherService searcherService = ApplicationContextManagerMock.getBean(ISearcherService.class);
        when(searcherService.doSearch(any(Search.class))).thenReturn(search);
        Search search = searcher.call();
        assertEquals(this.search, search);
    }

}