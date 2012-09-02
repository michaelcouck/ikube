package ikube.gui.panel;

import ikube.gui.Application;
import ikube.gui.IConstant;
import ikube.gui.data.IContainer;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class IndexesPanel extends Panel {

	private static final String NAME_COLUMN = "Name";
	private static final String DOCUMENTS_COLUMN = "Docs";
	private static final String SIZE_COLUMN = "Size";
	private static final String OPEN_COLUMN = "Open";
	private static final String MAX_AGE_COLUMN = "Max age";
	private static final String TIMESTAMP_COLUMN = "Timestamp";
	private static final String PATH_COLUMN = "Path";
	private static final String ACTION_COLUMN = "Action";

	private Resource nameIcon;
	private Resource documentsIcon;
	private Resource sizeIcon;
	private Resource openIcon;
	private Resource maxAgeIcon;
	private Resource timestampIcon;
	private Resource pathIcon;
	private Resource actionIcon;

	public IndexesPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(Boolean.TRUE);

		createIcons();
		TreeTable treeTable = createTreeTable();

		addComponent(treeTable);
	}

	private TreeTable createTreeTable() {
		TreeTable treeTable = new TreeTable(IConstant.INDEXES);
		treeTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		treeTable.setSelectable(Boolean.TRUE);
		treeTable.setImmediate(Boolean.TRUE);
		treeTable.setSortDisabled(Boolean.TRUE);

		treeTable.addContainerProperty(NAME_COLUMN, String.class, NAME_COLUMN, NAME_COLUMN, nameIcon, null);
		treeTable.addContainerProperty(DOCUMENTS_COLUMN, String.class, DOCUMENTS_COLUMN, DOCUMENTS_COLUMN, documentsIcon, null);
		treeTable.addContainerProperty(SIZE_COLUMN, String.class, SIZE_COLUMN, SIZE_COLUMN, sizeIcon, null);
		treeTable.addContainerProperty(OPEN_COLUMN, String.class, OPEN_COLUMN, OPEN_COLUMN, openIcon, null);
		treeTable.addContainerProperty(MAX_AGE_COLUMN, String.class, MAX_AGE_COLUMN, MAX_AGE_COLUMN, maxAgeIcon, null);
		treeTable.addContainerProperty(TIMESTAMP_COLUMN, String.class, TIMESTAMP_COLUMN, TIMESTAMP_COLUMN, timestampIcon, null);
		treeTable.addContainerProperty(PATH_COLUMN, String.class, PATH_COLUMN, PATH_COLUMN, pathIcon, null);
		treeTable.addContainerProperty(ACTION_COLUMN, Component.class, ACTION_COLUMN, ACTION_COLUMN, actionIcon, null);

		treeTable.setDescription(IConstant.INDEXES_TREE_TABLE);

		return treeTable;
	}

	private void createIcons() {
		nameIcon = new ClassResource(this.getClass(), "/images/icons/index.gif", Application.getApplication());
		documentsIcon = new ClassResource(this.getClass(), "/images/icons/web.gif", Application.getApplication());
		sizeIcon = new ClassResource(this.getClass(), "/images/icons/index_performance.gif", Application.getApplication());
		openIcon = new ClassResource(this.getClass(), "/images/icons/open.gif", Application.getApplication());
		maxAgeIcon = new ClassResource(this.getClass(), "/images/icons/progress_task.gif", Application.getApplication());
		timestampIcon = new ClassResource(this.getClass(), "/images/icons/register_view.gif", Application.getApplication());
		pathIcon = new ClassResource(this.getClass(), "/images/icons/memory_view.gif", Application.getApplication());
		actionIcon = new ClassResource(this.getClass(), "/images/icons/launch_run.gif", Application.getApplication());

	}

	public void setData(final Object data) {
		((IContainer) data).setData(this);
	}

}
