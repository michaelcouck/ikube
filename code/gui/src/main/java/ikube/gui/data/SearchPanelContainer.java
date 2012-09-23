package ikube.gui.data;

import ikube.IConstants;
import ikube.gui.IConstant;
import ikube.gui.toolkit.GuiTools;
import ikube.service.IMonitorService;
import ikube.service.ISearcherService;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TreeTable;

@Configurable
public class SearchPanelContainer extends HierarchicalContainer implements IContainer {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchPanelContainer.class);

	@Autowired
	private transient IMonitorService monitorService;
	@Autowired
	private transient ISearcherService searcherService;

	public void setData(final Panel panel, final Object... parameters) {
		ComboBox optionGroup = (ComboBox) GuiTools.findComponent(panel, IConstant.INDEXES_OPTION_GROUP, new ArrayList<Component>());
		String[] indexNames = monitorService.getIndexNames();
		for (String indexName : indexNames) {
			if (!optionGroup.containsId(indexName)) {
				optionGroup.addItem(indexName);
			}
		}
		if (parameters == null || parameters.length < 1) {
			return;
		}
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
		// Do the search on the indexes, preferably in parallel
		String[] searchStrings = new String[] { (String) parameters[1] };
		int firstResult = (Integer) parameters[2];
		int maxResults = (Integer) parameters[3];
		ArrayList<HashMap<String, String>> results = searcherService
				.searchMultiAll(indexName, searchStrings, true, firstResult, maxResults);
		// LOGGER.info("First : " + firstResult + ", max results : " + maxResults + ", search string : " +
		// Arrays.deepHashCode(searchStrings)
		// + ", results : " + results);
		// Set the resultant data in the table
		for (int i = 0; i < results.size() - 1; i++) {
			HashMap<String, String> result = results.get(i);
			Object[] cells = new Object[] { result.get(IConstants.INDEX), result.get(IConstants.ID), result.get(IConstants.SCORE),
					result.get(IConstants.FRAGMENT) };
			treeTable.addItem(cells, result.get(IConstants.INDEX));
		}
		HashMap<String, String> statistics = results.get(results.size() - 1);
		Label timeTakenLabel = (Label) GuiTools.findComponent(treeTable, IConstant.TIME_TAKEN, new ArrayList<Component>());
		setLabelValue(timeTakenLabel, statistics.get(IConstants.DURATION));

		Label totalResultsLabel = (Label) GuiTools.findComponent(treeTable, IConstant.TOTAL_RESULTS, new ArrayList<Component>());
		setLabelValue(totalResultsLabel, statistics.get(IConstants.TOTAL));
	}

	private void setLabelValue(final Label label, Object... values) {
		StringBuilder stringBuilder = new StringBuilder();
		Object labelData = label.getData();
		stringBuilder.append(labelData);
		for (Object value : values) {
			stringBuilder.append(value);
		}
		label.setValue(stringBuilder.toString());
	}
}