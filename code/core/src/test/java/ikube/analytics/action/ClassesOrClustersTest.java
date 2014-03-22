package ikube.analytics.action;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.IAnalyticsService;
import ikube.analytics.IAnalyzer;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Analysis;
import ikube.toolkit.ApplicationContextManager;
import junit.framework.Assert;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class ClassesOrClustersTest extends AbstractTest {

    private Analysis analysis;
    private ClassesOrClusters sizesForClassesOrClusters;

    @Before
    public void before() {
        analysis = mock(Analysis.class);
        Object[] classesOrClusters = new Object[]{IConstants.POSITIVE, IConstants.NEGATIVE};
        when(analysis.getClassesOrClusters()).thenReturn(classesOrClusters);
        sizesForClassesOrClusters = new ClassesOrClusters(analysis);

        IAnalyticsService analyticsService = mock(IAnalyticsService.class);
        IAnalyzer analyzer = mock(IAnalyzer.class);
        when(analyticsService.getAnalyzer(any(String.class))).thenReturn(analyzer);
        ApplicationContextManagerMock.setBean(IAnalyticsService.class, analyticsService);

        Mockit.setUpMocks(ApplicationContextManagerMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManager.class);
    }

    @Test
    public void call() throws Exception {
        Analysis analysis = sizesForClassesOrClusters.call();
        Assert.assertEquals(this.analysis, analysis);
        verify(analysis, atLeastOnce()).setClassesOrClusters(any(Object[].class));
    }

}