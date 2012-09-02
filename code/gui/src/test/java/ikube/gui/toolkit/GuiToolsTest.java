package ikube.gui.toolkit;

import static org.junit.Assert.assertNotNull;
import ikube.gui.Application;
import ikube.gui.IConstant;
import ikube.gui.data.IContainer;
import ikube.gui.panel.SearchPanel;
import ikube.toolkit.Logging;

import java.util.ArrayList;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class GuiToolsTest {

	static {
		Logging.configure();
	}

	private IContainer container;
	private Application application;

	@Before
	public void before() {
		container = Mockito.mock(IContainer.class);
		application = Mockito.mock(Application.class);
		Deencapsulation.setField(Application.class, application);
	}

	@Test
	public void findComponent() {
		Panel searchPanel = new SearchPanel(container);
		TreeTable treeTable = (TreeTable) GuiTools.findComponent((Component) searchPanel, IConstant.SEARCH_PANEL_TREE_TABLE,
				new ArrayList<Component>());
		assertNotNull(treeTable);
	}

}
