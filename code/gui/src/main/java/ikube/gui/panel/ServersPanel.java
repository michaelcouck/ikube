package ikube.gui.panel;

import ikube.gui.IConstant;
import ikube.gui.data.IContainer;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class ServersPanel extends Panel {

	private TreeTable treeTable;

	public ServersPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(Boolean.TRUE);

		treeTable = new TreeTable(IConstant.SERVERS);
		treeTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		treeTable.setSelectable(Boolean.TRUE);
		treeTable.setImmediate(Boolean.TRUE);
		treeTable.setSortDisabled(Boolean.TRUE);

		addComponent(treeTable);
	}

	public void setData(Object data) {
		((IContainer) data).init(this);
	}

}