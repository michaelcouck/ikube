package ikube.gui.handler;

import ikube.gui.Window;
import ikube.gui.panel.DashPanel;
import ikube.toolkit.ThreadUtilities;

import com.vaadin.data.Container;
import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressIndicator;

public class DashPanelHandler extends AHandler {

	protected void registerHandlerInternal(final Component component, final Container container) {
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
					((DashPanel) component).setData(container);
				}
			}
		});
	}

}
