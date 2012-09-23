package ikube.cluster.hzc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.database.IDataBase;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableFileSystem;

import java.util.ArrayList;
import java.util.Arrays;

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

	@SuppressWarnings("unchecked")
	@Test
	public void indexablesEqual() {
		IndexContext<?> indexContextOne = new IndexContext<Object>();
		IndexContext<?> indexContextTwo = new IndexContext<Object>();

		indexContextOne.setName("one");
		indexContextTwo.setName("two");

		Indexable<?> indexableOne = new IndexableFileSystem();
		Indexable<?> indexableTwo = new IndexableFileSystem();
		indexableOne.setName("one");
		indexableTwo.setName("two");

		indexContextOne.setChildren(new ArrayList<Indexable<?>>(Arrays.asList(indexableOne)));

		boolean equal = indexContextListener.indexablesEqual(indexContextOne, indexContextTwo);
		assertFalse(equal);

		indexContextTwo.setName("one");
		equal = indexContextListener.indexablesEqual(indexContextOne, indexContextTwo);
		assertFalse(equal);

		indexContextTwo.setChildren(new ArrayList<Indexable<?>>(Arrays.asList(indexableTwo)));
		equal = indexContextListener.indexablesEqual(indexContextOne, indexContextTwo);
		assertFalse(equal);

		indexableTwo.setName("one");
		equal = indexContextListener.indexablesEqual(indexContextOne, indexContextTwo);
		assertTrue(equal);
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