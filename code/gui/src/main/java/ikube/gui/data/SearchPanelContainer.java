package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.Application;
import ikube.gui.toolkit.GuiTools;
import ikube.service.IMonitorService;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

@Configurable
public class SearchPanelContainer extends HierarchicalContainer implements IContainer {

	private static final String INDEX_COLUMN = "Index";
	private static final String SCORE_COLUMN = "Score";
	private static final String FRAGMENT_COLUMN = "Fragment";
	private static final String FIELD_COLUMN = "Field";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchPanelContainer.class);

	private Resource indexIcon;
	private Resource scoreIcon;
	private Resource fragmentIcon;
	private Resource fieldIcon;

	@Autowired
	private transient IClusterManager clusterManager;
	@Autowired
	private transient IMonitorService monitorService;

	public void init() {
	}

	public void init(final Panel panel) {
		TreeTable treeTable = GuiTools.findComponent(panel, TreeTable.class);
		indexIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		scoreIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		fragmentIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		fieldIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());

		treeTable.addContainerProperty(INDEX_COLUMN, String.class, null, INDEX_COLUMN, indexIcon, null);
		treeTable.addContainerProperty(SCORE_COLUMN, String.class, null, SCORE_COLUMN, scoreIcon, null);
		treeTable.addContainerProperty(FRAGMENT_COLUMN, Long.class, null, FRAGMENT_COLUMN, fragmentIcon, null);
		treeTable.addContainerProperty(FIELD_COLUMN, Date.class, null, FIELD_COLUMN, fieldIcon, null);

		setData(treeTable);
	}

	private void setData(final TreeTable treeTable) {
	}

}