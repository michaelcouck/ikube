package ikube.gui.handler;

import ikube.gui.IConstant;
import ikube.gui.data.IContainer;
import ikube.gui.panel.MenuPanel;
import ikube.gui.toolkit.GuiTools;

import java.util.ArrayList;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.Tab;

@Deprecated
public class MenuPanelHandler extends AHandler {

	private Panel currentPanel;

	@Override
	protected void registerHandlerInternal(final Component component, final IContainer container) {
		currentPanel = (Panel) GuiTools.findComponent(component, IConstant.DASH, new ArrayList<Component>());
		MenuPanel menuPanel = (MenuPanel) GuiTools.findComponent(component, IConstant.MENU, new ArrayList<Component>());
		TabSheet tabSheet = (TabSheet) GuiTools.findComponent(menuPanel, IConstant.TAB_SHEET, new ArrayList<Component>());
		tabSheet.addListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component tab = event.getTabSheet().getSelectedTab();
				Component component = event.getComponent();
				Object source = event.getSource();
				logger.info("Event : " + event);
				logger.info("Event : " + tab + ", " + component + ", " + source);
				
				int tabIndex = event.getTabSheet().getTabIndex();
				logger.info("Event : " + tabIndex);
				
				if (component != null && Tab.class.isAssignableFrom(component.getClass())) {
					String description = ((Tab) component).getDescription();
					Panel newPanel = findPanel(description);
					logger.info("Panel : " + currentPanel + ", " + newPanel + ", " + description);
					if (currentPanel == null || newPanel == null) {
						logger.warn("Panel : " + currentPanel + ", " + newPanel + ", " + description);
						return;
					}
					switchPanel(currentPanel, newPanel);
					currentPanel = newPanel;
					newPanel.requestRepaintAll();
				}
			}
		});
	}

}
