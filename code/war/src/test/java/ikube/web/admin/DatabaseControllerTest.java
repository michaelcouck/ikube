package ikube.web.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import ikube.IConstants;
import ikube.database.jpa.ADataBaseJpa;
import ikube.database.jpa.DataBaseJpa;
import ikube.model.Search;

import java.util.Arrays;
import java.util.List;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

/**
 * @author Michael Couck
 * @since 03.03.12
@version 01.00
 */
public class DatabaseControllerTest {

	@MockClass(realClass = ADataBaseJpa.class)
	public static class IDataBaseMock {
		@Mock()
		@SuppressWarnings("unchecked")
		public <T> List<T> find(Class<T> klass, int startIndex, int endIndex) {
			Search search = new Search();
			return (List<T>) Arrays.asList(search);
		}
	}

	/** Class under test. */
	private DatabaseController databaseController;

	@Before
	public void before() {
		Mockit.setUpMocks(IDataBaseMock.class);
		databaseController = new DatabaseController();
		Deencapsulation.setField(databaseController, new DataBaseJpa());
	}

	@Test
	public void entities() throws Exception {
		String targetView = "targetView";
		Model model = new ExtendedModelMap();
		String targetViewResult = databaseController.entities(targetView, Search.class.getName(), 0, 10, model);
		assertTrue(model.asMap().size() == 3);
		assertTrue(model.asMap().get(IConstants.ENTITIES) != null);
		assertTrue(model.asMap().get(IConstants.FIELD_NAMES) != null);
		assertEquals("The redirect view has the redirect suffix : ", "redirect:" + targetView, targetViewResult);
	}

}
