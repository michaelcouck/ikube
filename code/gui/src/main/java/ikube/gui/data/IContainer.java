package ikube.gui.data;

import com.vaadin.data.Container;
import com.vaadin.ui.Panel;

public interface IContainer extends Container {

	public void setData(final Panel target, final Object... parameters);

}
