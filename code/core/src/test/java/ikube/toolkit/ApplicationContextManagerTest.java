package ikube.toolkit;

import ikube.ATest;
import ikube.database.IDataBase;
import ikube.model.IndexContext;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

/**
 * @author Michael Couck
 * @since 01.12.12
 * @version 01.00
 */
@Ignore
public class ApplicationContextManagerTest extends ATest {

	private ApplicationContextManager applicationContextManager;

	public ApplicationContextManagerTest() {
		super(ApplicationContextManagerTest.class);
	}

	@Before
	public void before() {
		applicationContextManager = new ApplicationContextManager();
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void setApplicationContext() {
		ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
		IDataBase dataBase = Mockito.mock(IDataBase.class);
		Mockito.when(dataBase.find(IndexContext.class, 0, Integer.MAX_VALUE)).thenReturn(
				new ArrayList<IndexContext>(indexContexts.values()));
		Mockito.when(applicationContext.getBean(IDataBase.class)).thenReturn(dataBase);
		applicationContextManager.setApplicationContext(applicationContext);
	}

}
