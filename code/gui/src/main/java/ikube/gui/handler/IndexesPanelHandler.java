package ikube.gui.handler;

import ikube.gui.IConstant;
import ikube.gui.Window;
import ikube.gui.data.IContainer;
import ikube.gui.panel.IndexesPanel;
import ikube.toolkit.ThreadUtilities;

import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressIndicator;

public class IndexesPanelHandler extends AHandler {

	@Override
	protected void registerHandlerInternal(final Component component, final IContainer container) {
		// This poller will request changes from the server periodically
		ProgressIndicator pollingIndicator = new ProgressIndicator();
		pollingIndicator.setPollingInterval(IConstant.REFRESH_INTERVAL);
		pollingIndicator.setVisible(Boolean.TRUE);
		pollingIndicator.setIndeterminate(Boolean.TRUE);
		pollingIndicator.setValidationVisible(Boolean.TRUE);

		Window window = Window.INSTANCE;
		window.addComponent(pollingIndicator);

		ThreadUtilities.submit(new Runnable() {
			public void run() {
				while (true) {
					ThreadUtilities.sleep(IConstant.REFRESH_INTERVAL);
					// LOGGER.info("Setting data : ");
					((IndexesPanel) component).setData(container);
				}
			}
		});
	}

}