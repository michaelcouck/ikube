package ikube.gui.handler;

import com.vaadin.data.Container;
import com.vaadin.ui.Component;

public interface IHandler {

	void registerHandler(final Component component, final Container container);

}
