package ikube.gui.toolkit;

import java.util.Iterator;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public final class GuiTools {

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

	private GuiTools() {
	}

}
