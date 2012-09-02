package ikube.gui.handler;

import ikube.gui.IConstant;
import ikube.gui.Window;
import ikube.gui.data.IContainer;
import ikube.gui.panel.ServersPanel;
import ikube.gui.toolkit.GuiTools;
import ikube.toolkit.ThreadUtilities;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;

public class ServersPanelHandler extends AHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServersPanelHandler.class);

	@Override
	protected void registerHandlerInternal(final Component component, final IContainer container) {
		Table treeTable = (Table) GuiTools.findComponent(component, IConstant.SERVERS_PANEL_TABLE, new ArrayList<Component>());
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
					try {
						ThreadUtilities.sleep(interval);
						LOGGER.info("Setting data : ");
						((ServersPanel) component).setData(container);
					} catch (Exception e) {
						LOGGER.error("Exception setting the data in the servers table : ", e);
					}
				}
			}
		});
	}

	private void addTreeTableListener(final Table treeTable) {
		treeTable.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				LOGGER.info("Event : " + event);
			}
		});
	}

}