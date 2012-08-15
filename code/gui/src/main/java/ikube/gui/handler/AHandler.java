package ikube.gui.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public abstract class AHandler implements IHandler {

	protected static final List<Panel> PANELS = new ArrayList<Panel>();

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public void registerHandler(final Component component, final Container container) {
		if (Panel.class.isAssignableFrom(component.getClass())) {
			PANELS.add((Panel) component);
		}
		registerHandlerInternal(component, container);
	}

	abstract void registerHandlerInternal(final Component component, final Container container);

	protected void switchPanel(final Component oldComponent, final Component newComponent) {
		if (oldComponent == null || newComponent == null) {
			return;
		}
		((ComponentContainer) oldComponent.getParent()).replaceComponent(oldComponent, newComponent);
	}

	protected Window findMainWindow(final Component component) {
		if (component == null) {
			return null;
		}
		if (Window.class.isAssignableFrom(component.getClass())) {
			return (Window) component;
		}
		return findMainWindow(component.getParent());
	}

	protected Panel findPanel(final Object description) {
		for (Panel panel : PANELS) {
			if (description.equals(panel.getDescription())) {
				return panel;
			}
		}
		return null;
	}

	protected Component findComponent(final Component component, final Object description, final List<Component> done) {
		if (component == null || done.contains(component)) {
			return null;
		}
		// logger.info("Component : " + component);
		done.add(component);
		if (AbstractComponent.class.isAssignableFrom(component.getClass())) {
			if (description.equals(((AbstractComponent) component).getDescription())) {
				return component;
			}
		}
		if (ComponentContainer.class.isAssignableFrom(component.getClass())) {
			Iterator<Component> componentIterator = ((ComponentContainer) component).getComponentIterator();
			while (componentIterator.hasNext()) {
				Component childComponent = componentIterator.next();
				if (findComponent(childComponent, description, done) != null) {
					return childComponent;
				}
			}
		}
		return findComponent(component.getParent(), description, done);
	}

}