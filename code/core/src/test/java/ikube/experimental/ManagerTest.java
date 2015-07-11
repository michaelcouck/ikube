package ikube.experimental;

import com.jcraft.jsch.JSchException;
import ikube.cluster.gg.ClusterManagerGridGain;
import ikube.cluster.listener.IListener;
import ikube.mock.SpellingCheckerMock;
import ikube.search.spelling.SpellingChecker;
import junit.framework.Assert;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
public class ManagerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private Manager manager;

    @Mock
    private Writer writer;
    @Mock
    private Database database;
    @Mock
    private ClusterManagerGridGain clusterManager;

    @Before
    public void before() {
        Mockit.setUpMock(SpellingChecker.class, SpellingCheckerMock.class);
    }

    @After
    public void after() {
        // Mockit.tearDownMocks();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addTopicListener() throws IOException {
        final AtomicReference<Object> atomicReference = new AtomicReference<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                atomicReference.set(arguments[1]);
                logger.error("Invocation : " + invocation);
                return null;
            }
        }).when(clusterManager).addTopicListener(Mockito.anyString(), any(IListener.class));
        manager.addTopicListener();
        // Fire an event to the listener
        ((IListener) atomicReference.get()).onMessage(new Document());
        verify(manager, times(1)).writeToIndex(any(Document.class));
    }

    @Test
    public void writeToIndex() throws IOException {
        Document document = new Document();
        manager.writeToIndex(document);
        verify(manager, times(1)).writeToIndex(any(Document.class));
    }

    @Test
    public void openSearcher() throws IOException, ParseException {
        when(writer.getDirectories()).thenReturn(getDirectories(3));
        for (int i = 1000; i >= 0; i--) {
            ArrayList<HashMap<String, String>> results;

            manager.openSearcher();

            results = manager.doSearch("float-0", "0.0");
            Assert.assertEquals(2, results.size());
            results = manager.doSearch("float-2", "2.0");
            Assert.assertEquals(2, results.size());
        }
    }

    @Test
    public void indexRecords() throws SQLException, JSchException {
        List<List<Object>> records = Arrays.asList(
                Arrays.asList((Object) "one", "two", "three"),
                Arrays.asList((Object) "two", "three", "four"),
                Arrays.asList((Object) "three", "four", "five"));
        List<Document> documents = Arrays.asList(new Document(), new Document(), new Document());
        when(database.readChangedRecords()).thenReturn(records);
        when(writer.createDocuments(records)).thenReturn(documents);
        manager.indexRecords();
        verify(clusterManager, times(3)).send(anyString(), any(Document.class));
    }

}