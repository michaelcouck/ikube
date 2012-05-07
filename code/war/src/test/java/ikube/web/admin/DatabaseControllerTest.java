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
import ikube.model.Action;
import ikube.model.Search;

import java.util.Arrays;
import java.util.List;

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
	private IDataBase dataBase = mock(IDataBase.class);
	private IClusterManager clusterManager = mock(IClusterManager.class);
	private String targetView = "targetView";
	private ModelAndView modelAndView = new ModelAndView();

	@Before
	public void before() {
		databaseController = new DatabaseController();
		Deencapsulation.setField(databaseController, dataBase);
		Deencapsulation.setField(databaseController, clusterManager);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void selectWithSort() throws Exception {
		when(dataBase.find(any(Class.class), anyInt(), anyInt())).thenReturn(Arrays.asList(new Search()));

		List<String> sortFields = Arrays.asList("id");
		List<Boolean> directionOfSort = Arrays.asList(Boolean.TRUE);
		ModelAndView returnModel = databaseController.selectWithSort(targetView, Search.class.getName(), sortFields, directionOfSort, 0,
				10, modelAndView);
		assertEquals(4, modelAndView.getModel().size());
		assertTrue(modelAndView.getModel().get(IConstants.ENTITIES) != null);
		assertTrue(modelAndView.getModel().get(IConstants.FIELD_NAMES) != null);
		assertEquals("The view name should be set in the model : ", targetView, returnModel.getViewName());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void selectWithFiltering() throws Exception {
		Action action = new Action();
		when(dataBase.findCriteria(any(Class.class), any(String[].class), any(Object[].class), anyInt(), anyInt())).thenReturn(
				Arrays.asList(action));

		List<String> fieldsToFilterOn = Arrays.asList("filteredField");
		List<Object> valuesToFilterOn = Arrays.asList((Object) "filteredValue");
		databaseController
				.selectWithFiltering(targetView, Action.class.getName(), fieldsToFilterOn, valuesToFilterOn, 0, 100, modelAndView);
		// TODO Validate something!(any(Class.class), anyInt(), anyInt())
		List<Action> actions = (List<Action>) modelAndView.getModel().get(IConstants.ENTITIES);
		assertTrue(actions.contains(action));
	}

}