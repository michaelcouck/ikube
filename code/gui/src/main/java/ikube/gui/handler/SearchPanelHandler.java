package ikube.gui.handler;

import ikube.gui.IConstant;
import ikube.gui.Window;
import ikube.gui.data.IContainer;
import ikube.gui.toolkit.GuiTools;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;

public class SearchPanelHandler extends AHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchPanelHandler.class);

	@Override
	protected void registerHandlerInternal(final Component component, final IContainer container) {
		TreeTable treeTable = (TreeTable) GuiTools.findComponent(component, IConstant.SEARCH_PANEL_TREE_TABLE, new ArrayList<Component>());
		Button searchButton = (Button) GuiTools.findComponent(component, IConstant.SEARCH_BUTTON, new ArrayList<Component>());
		searchButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ComboBox comboBox = (ComboBox) GuiTools
						.findComponent(component, IConstant.INDEXES_OPTION_GROUP, new ArrayList<Component>());
				TextField textField = (TextField) GuiTools.findComponent(component, IConstant.SEARCH_FIELD, new ArrayList<Component>());
				Object selectedIndex = comboBox.getValue();
				Object searchString = textField.getValue();
				if (selectedIndex == null || searchString == null) {
					Window.INSTANCE.showNotification("Please select an index to search and input a search term");
					return;
				}
				logger.info("Selected index : " + selectedIndex + ", search string : " + searchString);
				container.setData((Panel) component, selectedIndex.toString(), searchString.toString());
			}
		});
		addTreeTableListener(treeTable);
	}

	private void addTreeTableListener(final TreeTable treeTable) {
		treeTable.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				LOGGER.info("Event : " + event);
			}
		});
	}

}