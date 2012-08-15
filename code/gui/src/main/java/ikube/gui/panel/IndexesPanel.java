package ikube.gui.panel;

import ikube.gui.data.IContainer;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class IndexesPanel extends Panel {

	private TreeTable treeTable;

	public IndexesPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);

		treeTable = new TreeTable("Indexes");
		treeTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		treeTable.setSelectable(Boolean.TRUE);
		treeTable.setImmediate(Boolean.TRUE);
		treeTable.setSortDisabled(Boolean.TRUE);

		addComponent(treeTable);
	}

	public void setData(final Object data) {
		((IContainer) data).init(this);
	}

}
