package com.vaadin.incubator.spring.ui.panel;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;

public class ContentPanel extends Panel {

	public ContentPanel() {
		setWidth(100, Sizeable.UNITS_PERCENTAGE);
		setHeight(100, Sizeable.UNITS_PERCENTAGE);

		Table table = new Table();
		table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		table.setHeight(100, Sizeable.UNITS_PERCENTAGE);
		table.setSortDisabled(true);
		table.setPageLength(7);

		addComponent(table);
	}

}
