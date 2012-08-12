package ikube.gui.panel;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.Container;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

@Configurable(preConstruction = true)
public class NavigationPanel extends Panel {

	private Tree tree;

	public NavigationPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);
		addNavigationTree();
	}

	private void addNavigationTree() {
		tree = new Tree();
		tree.setImmediate(true);
		addComponent(tree);
	}

	@Override
	public void setData(Object data) {
		tree.setContainerDataSource((Container) data);
		for (Iterator<?> it = tree.rootItemIds().iterator(); it.hasNext();) {
			tree.collapseItemsRecursively(it.next());
		}
	}

}