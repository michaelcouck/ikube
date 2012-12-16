package ikube.cluster.listener.hzc;

import ikube.ATest;
import ikube.cluster.listener.hzc.IndexContextListener;
import ikube.database.IDataBase;
import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class IndexContextListenerTest extends ATest {

	private IndexContextListener indexContextListener;

	public IndexContextListenerTest() {
		super(IndexContextListenerTest.class);
	}

	@Before
	public void before() {
		indexContextListener = new IndexContextListener();
	}

	@Test
	public void handleIndexable() {
		IDataBase dataBase = Mockito.mock(IDataBase.class);
		Deencapsulation.setField(indexContextListener, dataBase);
		// indexContextListener.handleIndexable(indexable);
	}

	@Test
	public void handleIndexContext() {
		// TODO Implement me
	}

}