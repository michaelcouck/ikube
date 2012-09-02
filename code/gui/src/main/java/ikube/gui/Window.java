package ikube.gui;

import ikube.gui.data.DashPanelContainer;
import ikube.gui.data.IndexOptionsContainer;
import ikube.gui.data.IndexPanelContainer;
import ikube.gui.data.IndexesPanelContainer;
import ikube.gui.data.NavigationPanelContainer;
import ikube.gui.data.SearchPanelContainer;
import ikube.gui.data.ServersPanelContainer;
import ikube.gui.handler.DashPanelHandler;
import ikube.gui.handler.IndexPanelHandler;
import ikube.gui.handler.IndexesPanelHandler;
import ikube.gui.handler.NavigationPanelHandler;
import ikube.gui.handler.SearchPanelHandler;
import ikube.gui.handler.ServersPanelHandler;
import ikube.gui.panel.DashPanel;
import ikube.gui.panel.IndexPanel;
import ikube.gui.panel.IndexesPanel;
import ikube.gui.panel.MenuPanel;
import ikube.gui.panel.NavigationPanel;
import ikube.gui.panel.SearchPanel;
import ikube.gui.panel.ServersPanel;
import ikube.util.ApplicationObjectSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@Configurable
@Scope(value = "prototype")
@Component(value = "window")
public class Window extends com.vaadin.ui.Window {

	public static Window INSTANCE;

	@Autowired
	private transient DashPanelContainer dashPanelContainer;
	@Autowired
	private transient SearchPanelContainer searchPanelContainer;
	@Autowired
	private transient IndexPanelContainer indexPanelContainer;
	@Autowired
	private transient IndexesPanelContainer indexesPanelContainer;
	@Autowired
	private transient ApplicationObjectSupport applicationObjectSupport;
	@Autowired
	private transient NavigationPanelContainer navigationPanelContainer;
	@Autowired
	private transient ServersPanelContainer serversPanelContainer;
	@Autowired
	private transient IndexOptionsContainer indexOptionsContainer;

	public Window() {
		Window.INSTANCE = this;
		setName(IConstant.MAIN_WINDOW);
	}

	public void init() {
		// Our main layout is a horizontal layout
		VerticalLayout vertical = new VerticalLayout();
		vertical.setSizeFull();
		setContent(vertical);

		// Menu panel
		Panel menuPanel = new MenuPanel();
		menuPanel.setDescription(IConstant.MENU);
		vertical.addComponent(menuPanel);
		vertical.setExpandRatio(menuPanel, 0.11f);

		// Vertically divide the right area
		HorizontalSplitPanel horizontal = new HorizontalSplitPanel();
		horizontal.setSplitPosition(200, Sizeable.UNITS_PIXELS);
		vertical.addComponent(horizontal);
		vertical.setExpandRatio(horizontal, 0.89f);

		// Navigation tree to the left
		Panel navigationPanel = new NavigationPanel();
		navigationPanel.setDescription(IConstant.NAVIGATION);
		navigationPanel.setData(navigationPanelContainer);
		horizontal.addComponent(navigationPanel);

		// Content on the bottom
		Panel dashPanel = new DashPanel();
		dashPanel.setDescription(IConstant.DASH);
		dashPanel.setData(dashPanelContainer);
		horizontal.addComponent(dashPanel);

		Panel searchPanel = new SearchPanel(indexOptionsContainer);
		searchPanel.setDescription(IConstant.SEARCH);
		searchPanel.setData(searchPanelContainer);

		Panel indexesPanel = new IndexesPanel();
		indexesPanel.setDescription(IConstant.INDEXES);
		indexesPanel.setData(indexesPanelContainer);
		indexesPanel.setVisible(true);

		Panel indexPanel = new IndexPanel();
		indexPanel.setDescription(IConstant.INDEX);
		indexPanel.setData(indexPanelContainer);

		Panel serversPanel = new ServersPanel();
		serversPanel.setDescription(IConstant.SERVERS);
		serversPanel.setData(serversPanelContainer);

		// Add the controllers/listeners/handlers to the panels
		new NavigationPanelHandler().registerHandler(navigationPanel, navigationPanelContainer);
		new DashPanelHandler().registerHandler(dashPanel, dashPanelContainer);
		new SearchPanelHandler().registerHandler(searchPanel, searchPanelContainer);
		new IndexesPanelHandler().registerHandler(indexesPanel, indexesPanelContainer);
		new IndexPanelHandler().registerHandler(indexPanel, indexPanelContainer);
		new ServersPanelHandler().registerHandler(serversPanel, serversPanelContainer);
	}

}
