package com.vaadin.incubator.spring.ui.panel;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class TopPanel extends Panel {

	public TopPanel() {
		// setSizeFull();
		setWidth(100, Sizeable.UNITS_PERCENTAGE);
		setHeight(100, Sizeable.UNITS_PERCENTAGE);
		
		Label text = new Label("Some text", Label.CONTENT_XHTML);
		addComponent(text);
	}

}
