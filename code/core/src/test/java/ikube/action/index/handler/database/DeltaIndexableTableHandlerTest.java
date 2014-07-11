package ikube.action.index.handler.database;

import ikube.AbstractTest;
import ikube.cluster.IClusterManager;
import ikube.model.SavePoint;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
public class DeltaIndexableTableHandlerTest extends AbstractTest {

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
        Mockito.when(indexContext.isDelta()).thenReturn(Boolean.TRUE);
        deltaIndexableTableHandler.handleIndexableForked(indexContext, indexableTable);
        verify(clusterManager, atLeastOnce()).get(anyString(), anyString());
        verify(clusterManager, atLeastOnce()).put(anyString(), anyString(), any(SavePoint.class));
    }

}