package ikube.gui.panel;

import com.vaadin.data.Container;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class IndexPanel extends Panel {

	private TreeTable treeTable;

	public IndexPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);

		treeTable = new TreeTable();
		treeTable.setSizeFull();
		treeTable.setSortDisabled(true);
		treeTable.setPageLength(7);

		addComponent(treeTable);
	}

	public void setData(Object data) {
		treeTable.setContainerDataSource((Container) data);
	}

}