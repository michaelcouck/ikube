package ikube.analytics;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.action.*;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FILE;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import weka.classifiers.functions.SMO;
import weka.filters.Filter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static ikube.toolkit.FILE.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-11-2013
 */
public class AnalyticsServiceTest extends AbstractTest {

    @MockClass(realClass = Analyze.class)
    public static class AnalyzerMock {
        @mockit.Mock
        public Analysis call() throws Exception {
            return mock(Analysis.class);
        }
    }

    @MockClass(realClass = SizesForClassesOrClusters.class)
    public static class SizesForClassesOrClustersMock {
        @mockit.Mock
        public Analysis call() throws Exception {
            return mock(Analysis.class);
        }
    }

    @Mock
    private SMO smo;
    @Mock
    private Filter filter;
    @Mock
    private Future future;

    @Mock
    private Context context;
    @Mock
    private IAnalyzer analyzer;
    @Mock
    private Analysis<?, ?> analysis;

    @Mock
    private AnalyzerManager analyzerManager;

    @Spy
    @InjectMocks
    private AnalyticsService analyticsService;

    @Before
    @SuppressWarnings("unchecked")
    public void before() throws Exception {
        Mockit.setUpMocks(ApplicationContextManagerMock.class, AnalyzerMock.class, SizesForClassesOrClustersMock.class);

        when(context.getAnalyzer()).thenReturn(analyzer);
        when(context.getAlgorithms()).thenReturn(new Object[]{smo});
        when(context.getFilters()).thenReturn(new Object[]{filter});
        when(context.getFileNames()).thenReturn(new String[]{"sentiment-smo.arff"});

        Map<String, Context> contexts = new HashMap<>();
        contexts.put(context.getName(), context);
        when(analyticsService.getContexts()).thenReturn(contexts);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManager.class, AnalyzerMock.class, SizesForClassesOrClustersMock.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void create() throws Exception {
        context = analyticsService.create(context);
        assertNotNull(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Create.class));
    }

    @Test
    public void upload() {
        String fileName = "some-file.txt";
        File outputFile = null;
        try {
            boolean uploaded = analyticsService.upload("target/" + fileName, new ByteArrayInputStream("Some text here.".getBytes()));
            assertTrue(uploaded);
            outputFile = findFileRecursively(new File("."), fileName);
            assertNotNull(outputFile);
            assertTrue(outputFile.exists());
        } finally {
            FILE.deleteFile(outputFile);
        }
    }

    @Test
    public void data() {
        String fileName = "general.csv";
        File file = findFileRecursively(new File("."), fileName);

        setContents(new File(IConstants.ANALYTICS_DIRECTORY, fileName), getContent(file).getBytes());

        when(context.getFileNames()).thenReturn(new String[]{fileName});

        Object[][][] matrices = analyticsService.data(context, 10);
        assertNotNull(matrices);
        assertEquals(1, matrices.length);
        assertEquals(10, matrices[0].length);
        assertEquals(40, matrices[0][0].length);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void train() throws Exception {
        context = analyticsService.train(analysis);
        assertNotNull(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Train.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void build() throws Exception {
        context = analyticsService.build(analysis);
        assertNotNull(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Build.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void analyze() throws Exception {
        when(clusterManager.sendTask(any(Analyze.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(analysis);
        // when(analyzer.analyze(any(Context.class), any())).thenReturn(analysis);

        Analysis analysis = analyticsService.analyze(this.analysis);
        assertNotNull(analysis);

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        analyticsService.analyze(this.analysis);
        verify(clusterManager, atLeastOnce()).sendTask(any(Analyze.class));
        verify(future, atLeastOnce()).get(anyLong(), any(TimeUnit.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void sizesForClassesOrClusters() throws Exception {
        when(clusterManager.sendTask(any(Analyze.class))).thenReturn(future);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(analysis);

        Analysis analysis = analyticsService.sizesForClassesOrClusters(this.analysis);
        assertNotNull(analysis);

        when(this.analysis.isDistributed()).thenReturn(Boolean.TRUE);
        analyticsService.sizesForClassesOrClusters(this.analysis);
        verify(clusterManager, atLeastOnce()).sendTask(any(SizesForClassesOrClusters.class));
        verify(future, atLeastOnce()).get(anyLong(), any(TimeUnit.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void destroy() throws Exception {
        analyticsService.destroy(context);
        verify(clusterManager, atLeastOnce()).sendTaskToAll(any(Destroy.class));
    }

}