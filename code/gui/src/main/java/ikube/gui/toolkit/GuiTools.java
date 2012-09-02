package ikube.gui.toolkit;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public final class GuiTools {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(GuiTools.class);

	@SuppressWarnings("unchecked")
	public static <T extends Component> T findComponent(final Component component, final Class<T> klass) {
		Component result = null;
		if (ComponentContainer.class.isAssignableFrom(component.getClass())) {
			Iterator<Component> iterator = ((ComponentContainer) component).getComponentIterator();
			while (iterator.hasNext()) {
				Component child = iterator.next();
				if (klass.getName().equals(child.getClass().getName())) {
					result = child;
					break;
				}
			}
		}
		return (T) result;
	}

	public static final Component findComponent(final Component component, final Object description, final List<Component> done) {
		if (component == null || done.contains(component)) {
			return null;
		}
		// LOGGER.info("Component : " + component);
		done.add(component);
		if (AbstractComponent.class.isAssignableFrom(component.getClass())) {
			if (description.equals(((AbstractComponent) component).getDescription())) {
				return component;
			}
		}
		if (ComponentContainer.class.isAssignableFrom(component.getClass())) {
			Iterator<Component> componentIterator = ((ComponentContainer) component).getComponentIterator();
			while (componentIterator.hasNext()) {
				Component childComponent = componentIterator.next();
				Component foundComponent = findComponent(childComponent, description, done);
				if (foundComponent != null) {
					return foundComponent;
				}
			}
		}
		return findComponent(component.getParent(), description, done);
	}

	private GuiTools() {
	}

}
