package ikube.integration.action;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.action.Index;
import ikube.index.handler.database.IndexableTableHandler;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.Indexable;
import ikube.model.IndexableTable;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mockit.Deencapsulation;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Michael Couck
 * @since 14.01.12
 * @version 01.00
 */
public class IndexTest extends ATest {

	private Index index;

	public IndexTest() {
		super(IndexTest.class);
	}

	@Before
	public void before() throws Exception {
		index = new Index();
		ThreadUtilities.initialize();
		Mockit.setUpMocks(ApplicationContextManagerMock.class);

		Deencapsulation.setField(index, dataBase);
		Deencapsulation.setField(index, clusterManager);
	}

	@After
	public void after() {
		ThreadUtilities.destroy();
	}

	@Test
	public void execute() throws Exception {
		indexableTable = new IndexableTable();
		List<Indexable<?>> indexables = new ArrayList<Indexable<?>>(Arrays.asList(indexableTable));
		Mockito.when(indexContext.getIndexables()).thenReturn(indexables);
		boolean result = index.execute(indexContext);
		logger.info("Result from index action : " + result);
		assertTrue("The index must execute properly : ", result);
	}

	@Test
	public void getHandler() {
		IndexableTable indexableTable = new IndexableTable();
		Object handler = Deencapsulation.invoke(index, "getHandler", indexableTable);
		assertTrue("The handler for the Arabic datashould be the wiki handler : ",
				handler.getClass().getName().contains(IndexableTableHandler.class.getSimpleName()));
	}

}