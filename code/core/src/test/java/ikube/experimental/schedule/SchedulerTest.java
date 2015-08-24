package ikube.experimental.schedule;

import com.jcraft.jsch.JSchException;
import ikube.cluster.gg.ClusterManagerGridGain;
import ikube.experimental.AbstractTest;
import ikube.experimental.Context;
import ikube.experimental.connect.DatabaseConnector;
import ikube.experimental.schedule.Scheduler;
import ikube.experimental.write.Writer;
import ikube.mock.SpellingCheckerMock;
import ikube.search.spelling.SpellingChecker;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-07-2015
 */
public class SchedulerTest extends AbstractTest {

    @Spy
    @InjectMocks
    private Scheduler manager;

    @Mock
    private Writer writer;
    @Mock
    private DatabaseConnector database;
    @Mock
    private ClusterManagerGridGain clusterManager;

    @Before
    public void before() {
        Mockit.setUpMock(SpellingChecker.class, SpellingCheckerMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks();
    }

    @Test
    public void databaseSchedule() throws Exception {
        List<Context> contexts = Arrays.asList(new Context());
        Whitebox.setInternalState(manager, "contexts", contexts);
        manager.databaseSchedule();
        verify(clusterManager, times(1)).send(anyString(), any(Document.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addTopicListener() throws IOException {
        //final AtomicReference<Object> atomicReference = new AtomicReference<>();
        //doAnswer(new Answer() {
        //    @Override
        //    public Object answer(final InvocationOnMock invocation) throws Throwable {
        //        Object[] arguments = invocation.getArguments();
        //        atomicReference.set(arguments[1]);
        //        logger.error("Invocation : " + invocation);
        //        return null;
        //    }
        //}).when(clusterManager).addTopicListener(Mockito.anyString(), any(IListener.class));
        //manager.getWriter().addTopicListener();
        //// Fire an event to the listener
        //((IListener) atomicReference.get()).onMessage(new Document());
        //verify(manager, times(1)).getWriter().writeToIndex(any(Document.class));
    }

    @Test
    public void writeToIndex() throws IOException {
        //Document document = new Document();
        //manager.getWriter().writeToIndex(document);
        //verify(manager, times(1)).getWriter().writeToIndex(any(Document.class));
    }

    @Test
    public void openSearcher() throws IOException, ParseException {
        //when(writer.getDirectories()).thenReturn(getDirectories(3));
        //for (int i = 1000; i >= 0; i--) {
        //    ArrayList<HashMap<String, String>> results;
        //
        //    manager.getSearcher().openSearcher();
        //
        //    results = manager.getSearcher().doSearch("float-0", "0.0");
        //    Assert.assertEquals(2, results.size());
        //    results = manager.getSearcher().doSearch("float-2", "2.0");
        //    Assert.assertEquals(2, results.size());
        //}
    }

    @Test
    public void indexRecords() throws SQLException, JSchException {
        //List<Map<Object, Object>> records = Arrays.asList(
        //        getMap(new Object[] {"one", "two", "three"}, new Object[] {"one", "two", "three"}),
        //        getMap(new Object[] {"two", "three", "four"}, new Object[] {"two", "three", "four"}),
        //        getMap(new Object[] {"three", "four", "five"}, new Object[] {"three", "four", "five"})
        //);
        //
        //List<Document> documents = Arrays.asList(new Document(), new Document(), new Document());
        //when(database.readChangedRecords()).thenReturn(records);
        //when(writer.createDocuments(records)).thenReturn(documents);
        //manager.getWriter().indexRecords();
        //verify(clusterManager, times(3)).send(anyString(), any(Document.class));
    }

    private Map<Object, Object> getMap(final Object[] keys, final Object[] values) {
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

}