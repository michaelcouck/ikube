package ikube.gui.handler;

import ikube.gui.Window;
import ikube.gui.data.IContainer;
import ikube.gui.panel.IndexesPanel;
import ikube.gui.toolkit.GuiTools;
import ikube.toolkit.ThreadUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TreeTable;

public class IndexesPanelHandler extends AHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexesPanelHandler.class);

	@Override
	protected void registerHandlerInternal(final Component component, final IContainer container) {
		TreeTable treeTable = GuiTools.findComponent(component, TreeTable.class);
		addTreeTableListener(treeTable);

		final int interval = 10000;

		// This poller will request changes from the server periodically
		ProgressIndicator pollingIndicator = new ProgressIndicator();
		pollingIndicator.setPollingInterval(interval);
		pollingIndicator.setVisible(Boolean.FALSE);
		pollingIndicator.setIndeterminate(Boolean.TRUE);
		pollingIndicator.setValidationVisible(Boolean.FALSE);

		Window window = Window.INSTANCE;
		window.addComponent(pollingIndicator);

		ThreadUtilities.submit(new Runnable() {
			public void run() {
				while (true) {
					ThreadUtilities.sleep(interval);
					// LOGGER.info("Setting data : ");
					((IndexesPanel) component).setData(container);
				}
			}
		});
	}

	private void addTreeTableListener(final TreeTable treeTable) {
		treeTable.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				LOGGER.info("Event : " + event);
			}
		});
	}

}