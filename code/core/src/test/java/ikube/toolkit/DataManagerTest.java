package ikube.toolkit;

import ikube.AbstractTest;
import ikube.database.IDataBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06-04-2014
 */
public class DataManagerTest extends AbstractTest {

    @Mock
    private IDataBase dataBase;
    @Spy
    @InjectMocks
    private DataManager dataManager;

    @Before
    public void before() {
        Whitebox.setInternalState(dataManager, "dataBase", dataBase);
    }

    @Test
    public void loadCountries() {
        when(dataBase.count(any(Class.class))).thenReturn(0l);
        dataManager.loadCountries();
        verify(dataBase, times(1)).persistBatch(any(List.class));
        verify(dataBase, times(1)).mergeBatch(any(List.class));
    }

}
