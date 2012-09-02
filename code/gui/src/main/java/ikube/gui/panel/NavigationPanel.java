package ikube.gui.panel;

import ikube.gui.IConstant;
import ikube.gui.data.IContainer;

import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

@Configurable(preConstruction = true)
public class NavigationPanel extends Panel {

	public NavigationPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(Boolean.TRUE);
		addNavigationTree();
	}

	private void addNavigationTree() {
		Tree tree = new Tree();
		tree.setSizeFull();
		tree.setVisible(Boolean.TRUE);
		tree.setImmediate(Boolean.TRUE);
		tree.setValidationVisible(Boolean.TRUE);
		tree.setDescription(IConstant.NAVIGATION_TREE);

		addComponent(tree);
	}

	@Override
	public void setData(final Object data) {
		((IContainer) data).setData(this);
	}

}