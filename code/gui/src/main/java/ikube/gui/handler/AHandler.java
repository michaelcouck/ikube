package ikube.gui.handler;

import ikube.gui.Styler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public abstract class AHandler {

	protected static final List<Panel> PANELS = new ArrayList<Panel>();

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	public void addListener(final Panel panel) {
		PANELS.add(panel);
		addListenerInternal(panel);
	}

	abstract void addListenerInternal(final Panel panel);

	protected void switchPanel(final Component component, final Component componentToAdd) {
		switchPanel(component, componentToAdd, new ArrayList<Component>());
	}

	protected boolean switchPanel(final Component oldComponent, final Component newComponent, final List<Component> done) {
		if (oldComponent == null || done.contains(oldComponent)) {
			return false;
		}
		done.add(oldComponent);
		if (Panel.class.isAssignableFrom(oldComponent.getClass())) {
			if (ComponentContainer.class.isAssignableFrom(oldComponent.getParent().getClass())) {
				ComponentContainer parent = (ComponentContainer) oldComponent.getParent();
				logger.info("Switching from : " + oldComponent + ", to : " + newComponent);
				String styleName = oldComponent.getStyleName();
				Styler.setThemeAndStyle(styleName, newComponent);
				parent.replaceComponent(oldComponent, newComponent);
				return true;
			}
		}
		if (ComponentContainer.class.isAssignableFrom(oldComponent.getClass())) {
			Iterator<Component> componentIterator = ((ComponentContainer) oldComponent).getComponentIterator();
			while (componentIterator.hasNext()) {
				if (switchPanel(componentIterator.next(), newComponent, done)) {
					return true;
				}
			}
		}
		return switchPanel(oldComponent.getParent(), newComponent, done);
	}

	protected Window getMainWindow(final Component component) {
		if (component == null) {
			return null;
		}
		if (Window.class.isAssignableFrom(component.getClass())) {
			return (Window) component;
		}
		return getMainWindow(component.getParent());
	}

	protected Panel getPanel(final Object description) {
		for (Panel panel : PANELS) {
			if (panel.getDescription().equals(description)) {
				return panel;
			}
		}
		return null;
	}

}
