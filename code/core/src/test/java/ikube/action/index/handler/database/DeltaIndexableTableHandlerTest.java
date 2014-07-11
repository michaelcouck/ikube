package ikube.action.index.handler.database;

import ikube.AbstractTest;
import ikube.cluster.IClusterManager;
import ikube.model.SavePoint;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
public class DeltaIndexableTableHandlerTest extends AbstractTest {

    @Mock
    private SavePoint savePoint;
    @Mock
    private IClusterManager clusterManager;
    @Spy
    @InjectMocks
    private DeltaIndexableTableHandler deltaIndexableTableHandler;

    @Before
    public void before() {
        Deencapsulation.setField(deltaIndexableTableHandler, "clusterManager", clusterManager);
    }

    @Test
    public void handleIndexableForked() throws Exception {
        when(indexContext.isDelta()).thenReturn(Boolean.TRUE);
        when(savePoint.getIdentifier()).thenReturn(Long.MAX_VALUE);
        when(clusterManager.get(anyString(), anyString())).thenReturn(savePoint);
        deltaIndexableTableHandler.handleIndexableForked(indexContext, indexableTable);
        verify(clusterManager, atLeastOnce()).get(anyString(), anyString());
        verify(clusterManager, atLeastOnce()).put(anyString(), anyString(), any(SavePoint.class));
    }

}