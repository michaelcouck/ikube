package ikube.gui.panel;

import ikube.gui.IConstant;

import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;

public class DashPanel extends Panel {

	public DashPanel() {
		setSizeFull();
		setImmediate(true);

		Table table = new Table(IConstant.DASHBOARD);
		table.setSizeFull();
		table.setSortDisabled(true);
		table.setPageLength(7);

		addComponent(table);
	}

}
