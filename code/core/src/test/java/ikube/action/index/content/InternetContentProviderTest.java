package ikube.action.index.content;

import ikube.AbstractTest;
import ikube.model.IndexableInternet;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-11-10
 */
@SuppressWarnings("deprecation")
public class InternetContentProviderTest extends AbstractTest {

    @Before
    public void before() {
        internetContentProvider = new InternetContentProvider();
    }

    private InternetContentProvider internetContentProvider;

    @Test
    public void getContent() {
        IndexableInternet indexableInternet = mock(IndexableInternet.class);
        OutputStream outputStream = mock(OutputStream.class);
        internetContentProvider.getContent(indexableInternet, outputStream);
        verify(indexableInternet, atLeastOnce()).getMaxReadLength();
    }

}
