package ikube.gui.handler;

import ikube.gui.IConstant;
import ikube.gui.Window;
import ikube.gui.data.IContainer;
import ikube.gui.toolkit.GuiTools;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

public class SearchPanelHandler extends AHandler {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchPanelHandler.class);

	@Override
	protected void registerHandlerInternal(final Component component, final IContainer container) {
		Button searchButton = (Button) GuiTools.findComponent(component, IConstant.SEARCH_BUTTON, new ArrayList<Component>());
		final ComboBox comboBox = (ComboBox) GuiTools.findComponent(component, IConstant.INDEXES_OPTION_GROUP, new ArrayList<Component>());
		searchButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				TextField textField = (TextField) GuiTools.findComponent(component, IConstant.SEARCH_FIELD, new ArrayList<Component>());
				Object selectedIndex = comboBox.getValue();
				Object searchString = textField.getValue();
				if (StringUtils.isEmpty((String) selectedIndex) || StringUtils.isEmpty((String) searchString)) {
					Window.INSTANCE.showNotification("Please select an index to search and input a search term");
					return;
				}
				// logger.info("Selected index : " + selectedIndex + ", search string : " + searchString);
				container.setData((Panel) component, selectedIndex.toString(), searchString.toString(), 0, 100);
			}
		});
		// TODO Add the paging buttons
	}

}