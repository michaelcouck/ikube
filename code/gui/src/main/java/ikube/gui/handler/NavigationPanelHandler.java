package ikube.gui.handler;

import ikube.gui.IConstant;
import ikube.gui.Window;
import ikube.gui.data.IContainer;
import ikube.gui.panel.NavigationPanel;
import ikube.gui.toolkit.GuiTools;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;
import java.util.Iterator;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Tree;

public class NavigationPanelHandler extends AHandler {

	private Panel currentPanel;

	@Override
	protected void registerHandlerInternal(final Component component, final IContainer container) {
		// The dash board is always the first panel in the center
		currentPanel = (Panel) GuiTools.findComponent(component, IConstant.DASH, new ArrayList<Component>());
		Iterator<Component> iterator = ((Panel) component).getComponentIterator();
		while (iterator.hasNext()) {
			Component childComponent = iterator.next();
			if (Tree.class.isAssignableFrom(childComponent.getClass())) {
				addTreeListener((Tree) childComponent);
				break;
			}
		}

		final int interval = 10000;

		// This poller will request changes from the server periodically
		ProgressIndicator pollingIndicator = new ProgressIndicator();
		pollingIndicator.setVisible(Boolean.FALSE);
		pollingIndicator.setIndeterminate(Boolean.TRUE);
		pollingIndicator.setValidationVisible(Boolean.FALSE);
		pollingIndicator.setPollingInterval(interval);

		Window window = Window.INSTANCE;
		window.addComponent(pollingIndicator);

		ThreadUtilities.submit(new Runnable() {
			public void run() {
				while (true) {
					// LOGGER.info("Setting data : ");
					((NavigationPanel) component).setData(container);
					ThreadUtilities.sleep(interval);
				}
			}
		});

	}

	private void addTreeListener(final Tree tree) {
		tree.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Property property = event.getProperty();
				Object description = property.getValue();
				Panel newPanel = findPanel(description);
				if (currentPanel == null || newPanel == null) {
					logger.warn("Panel : " + currentPanel + ", " + newPanel + ", " + description);
					return;
				}
				switchPanel(currentPanel, newPanel);
				currentPanel = newPanel;
				newPanel.requestRepaintAll();
			}
		});
	}

}