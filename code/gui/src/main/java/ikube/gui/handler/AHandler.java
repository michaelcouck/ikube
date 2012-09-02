package ikube.gui.handler;

import ikube.gui.data.IContainer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;

public abstract class AHandler implements IHandler {

	protected static final List<Panel> PANELS = new ArrayList<Panel>();

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public void registerHandler(final Component component, final IContainer container) {
		if (Panel.class.isAssignableFrom(component.getClass())) {
			PANELS.add((Panel) component);
		}
		registerHandlerInternal(component, container);
	}

	abstract void registerHandlerInternal(final Component component, final IContainer container);

	protected void switchPanel(final Component oldComponent, final Component newComponent) {
		if (oldComponent == null || newComponent == null) {
			return;
		}
		((ComponentContainer) oldComponent.getParent()).replaceComponent(oldComponent, newComponent);
	}

	protected Panel findPanel(final Object description) {
		for (Panel panel : PANELS) {
			if (description.equals(panel.getDescription())) {
				return panel;
			}
		}
		return null;
	}

}