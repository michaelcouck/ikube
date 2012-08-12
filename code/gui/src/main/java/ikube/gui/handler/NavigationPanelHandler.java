package ikube.gui.handler;

import java.util.Iterator;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

public class NavigationPanelHandler extends AHandler {

	private Panel panel;

	public NavigationPanelHandler(final Panel panel) {
		this.panel = panel;
	}

	protected void addListenerInternal(final Panel panel) {
		Iterator<Component> iterator = panel.getComponentIterator();
		while (iterator.hasNext()) {
			Component component = iterator.next();
			if (Tree.class.isAssignableFrom(component.getClass())) {
				addTreeListener((Tree) component);
				break;
			}
		}
	}

	private void addTreeListener(final Tree tree) {
		tree.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Property property = event.getProperty();
				Object description = property.getValue();
				Panel newPanel = getPanel(description);
				if (newPanel == null) {
					logger.warn("New panel null : ");
					return;
				}
				switchPanel(panel, newPanel);
				panel = newPanel;
			}
		});
	}

}