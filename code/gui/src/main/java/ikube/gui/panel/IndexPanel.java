package ikube.gui.panel;

import com.vaadin.data.Container;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class IndexPanel extends Panel {

	private TreeTable treeTable;

	public IndexPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(Boolean.TRUE);

		treeTable = new TreeTable();
		treeTable.setSizeFull();
		treeTable.setPageLength(10);
		treeTable.setSortDisabled(Boolean.TRUE);

		addComponent(treeTable);
	}

	public void setData(Object data) {
		treeTable.setContainerDataSource((Container) data);
	}

}