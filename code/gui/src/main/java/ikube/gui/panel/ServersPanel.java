package ikube.gui.panel;

import ikube.gui.IConstant;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class ServersPanel extends Panel {

	public ServersPanel() {
		setSizeFull();
		setImmediate(true);

		Label text = new Label(IConstant.SERVERS, Label.CONTENT_XHTML);
		addComponent(text);

		TreeTable treeTable = new TreeTable();
		treeTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		treeTable.setHeight(100, Sizeable.UNITS_PERCENTAGE);
		treeTable.setSortDisabled(true);
		treeTable.setPageLength(7);

		addComponent(treeTable);
	}

}
