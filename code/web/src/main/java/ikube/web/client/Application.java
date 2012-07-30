package ikube.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button saveOrUpdateButton = new Button("SaveOrUpdate");
		RootPanel.get("updateEmployeeButtonContainer").add(saveOrUpdateButton);
	}

}
