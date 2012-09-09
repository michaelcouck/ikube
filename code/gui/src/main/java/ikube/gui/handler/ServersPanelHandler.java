package ikube.gui.handler;

import ikube.gui.IConstant;
import ikube.gui.Window;
import ikube.gui.data.IContainer;
import ikube.gui.panel.ServersPanel;
import ikube.toolkit.ThreadUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressIndicator;

public class ServersPanelHandler extends AHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServersPanelHandler.class);

	@Override
	protected void registerHandlerInternal(final Component component, final IContainer container) {

		// This poller will request changes from the server periodically
		ProgressIndicator pollingIndicator = new ProgressIndicator();
		pollingIndicator.setPollingInterval(IConstant.REFRESH_INTERVAL);
		pollingIndicator.setVisible(Boolean.FALSE);
		pollingIndicator.setIndeterminate(Boolean.TRUE);
		pollingIndicator.setValidationVisible(Boolean.FALSE);

		Window window = Window.INSTANCE;
		window.addComponent(pollingIndicator);

		ThreadUtilities.submit(new Runnable() {
			public void run() {
				while (true) {
					try {
						ThreadUtilities.sleep(IConstant.REFRESH_INTERVAL);
						// LOGGER.info("Setting data : ");
						((ServersPanel) component).setData(container);
					} catch (Exception e) {
						LOGGER.error("Exception setting the data in the servers table : ", e);
					}
				}
			}
		});
	}

}