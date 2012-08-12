package ikube.gui.panel;

import ikube.gui.Application;

import com.vaadin.data.Container;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

public class IndexesPanel extends Panel {

	private TreeTable treeTable;

	public IndexesPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);

		treeTable = new TreeTable("Indexes");
		treeTable.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		treeTable.setSortDisabled(true);

		Resource columnIcon = new ClassResource(this.getClass(), "/images/Fotolia_5864227_XS.jpg", Application.getApplication());
		Object[] data = { "One", "Two", "Three", "Four", "Five" };
		for (int i = 0; i < data.length; i++) {
			String parentId = Integer.toString(i);
			String itemId = Integer.toString(i * 10);
			treeTable.addContainerProperty(data[i], String.class, data[i], Integer.toString(i), columnIcon, null);
			treeTable.addItem(new Object[] { "The", "quick", "brown", "fox", "jumped" }, parentId);
			treeTable.addItem(new Object[] { "The", "quick", "brown", "fox", "jumped" }, itemId);
			treeTable.setParent(itemId, parentId);
		}

		addComponent(treeTable);
	}

	public void setData(final Object data) {
		
	}

}
