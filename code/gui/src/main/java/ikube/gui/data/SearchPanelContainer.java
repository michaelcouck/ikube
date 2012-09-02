package ikube.gui.data;

import ikube.IConstants;
import ikube.gui.IConstant;
import ikube.gui.toolkit.GuiTools;
import ikube.service.IMonitorService;
import ikube.service.ISearcherService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

@Configurable
public class SearchPanelContainer extends HierarchicalContainer implements IContainer {

	@Autowired
	private transient IMonitorService monitorService;
	@Autowired
	private transient ISearcherService searcherService;

	public void init() {
	}

	public void setData(final Panel panel, final Object... parameters) {
		TreeTable treeTable = (TreeTable) GuiTools.findComponent(panel, IConstant.SEARCH_PANEL_TREE_TABLE, new ArrayList<Component>());
		setData(treeTable, parameters);
		treeTable.requestRepaint();
		treeTable.requestRepaintAll();
	}

	private void setData(final TreeTable treeTable, final Object... parameters) {
		for (Object itemId : treeTable.getItemIds()) {
			treeTable.removeItem(itemId);
		}
		String indexName = (String) parameters[0];
		// Get the indexes, and the search strings and any other parameters required
		String[] searchFields = monitorService.getIndexFieldNames((String) parameters[0]);
		// Do the search on the indexes, preferably in parallel
		String[] searchStrings = new String[searchFields.length];
		Arrays.fill(searchStrings, parameters[1]);
		ArrayList<HashMap<String, String>> results = searcherService.searchMulti(indexName, searchStrings, searchFields, true, 0, 10);
		// Set the resultant data in the table
		for (int i = 0; i < results.size() - 1; i++) {
			HashMap<String, String> result = results.get(i);
			Object[] cells = new Object[] { result.get(IConstants.INDEX), result.get(IConstants.ID), result.get(IConstants.SCORE),
					result.get(IConstants.FRAGMENT) };
			treeTable.addItem(cells, result.get(IConstants.INDEX));
		}
	}
}