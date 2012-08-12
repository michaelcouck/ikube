package ikube.gui;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public final class Styler {

	public static void setThemeAndStyle(final String theme, final String style, Collection<Window> windows) {
		for (Window window : windows) {
			window.setTheme(theme);
			setThemeAndStyle(style, window);
			setThemeAndStyle(style, window.getContent());
		}
	}

	public static void setThemeAndStyle(final String style, final Component component) {
		// Only set the style if it hasn't been set already
		if (StringUtils.isEmpty(component.getStyleName())) {
			component.setStyleName(style);
			component.addStyleName(style);
			if (Panel.class.isAssignableFrom(component.getClass())) {
				((Panel) component).getContent().setStyleName(style);
				((Panel) component).getContent().addStyleName(style);
			}
		}
		if (ComponentContainer.class.isAssignableFrom(component.getClass())) {
			Iterator<Component> componentIterator = ((ComponentContainer) component).getComponentIterator();
			while (componentIterator.hasNext()) {
				setThemeAndStyle(style, componentIterator.next());
			}
		}
		component.requestRepaint();
		component.requestRepaintRequests();
	}

}
