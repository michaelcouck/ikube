package ikube.gui.handler;

import ikube.gui.data.IContainer;

import com.vaadin.ui.Component;

public interface IHandler {

	void registerHandler(final Component component, final IContainer container);

}
