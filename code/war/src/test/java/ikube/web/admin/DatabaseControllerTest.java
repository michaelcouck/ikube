package ikube.web.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Search;

import java.util.Arrays;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 03.03.12
 * @version 01.00
 */
public class DatabaseControllerTest {

	/** Class under test. */
	private DatabaseController databaseController;

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		IDataBase dataBase = mock(IDataBase.class);
		IClusterManager clusterManager = mock(IClusterManager.class);
		databaseController = new DatabaseController();
		when(dataBase.find(any(Class.class), anyInt(), anyInt())).thenReturn(Arrays.asList(new Search()));
		Deencapsulation.setField(databaseController, dataBase);
		Deencapsulation.setField(databaseController, clusterManager);
	}

	@Test
	public void entities() throws Exception {
		String targetView = "targetView";
		ModelAndView model = new ModelAndView();
		ModelAndView returnModel = databaseController.entities(targetView, Search.class.getName(), 0, 10, model);
		assertEquals(4, model.getModel().size());
		assertTrue(model.getModel().get(IConstants.ENTITIES) != null);
		assertTrue(model.getModel().get(IConstants.FIELD_NAMES) != null);
		assertEquals("The view name should be set in the model : ", targetView, returnModel.getViewName());
	}

}
