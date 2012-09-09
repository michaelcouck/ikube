package ikube.gui;

import ikube.gui.data.DashPanelContainer;
import ikube.gui.data.IndexesPanelContainer;
import ikube.gui.data.SearchPanelContainer;
import ikube.gui.data.ServersPanelContainer;
import ikube.gui.handler.DashPanelHandler;
import ikube.gui.handler.IndexesPanelHandler;
import ikube.gui.handler.SearchPanelHandler;
import ikube.gui.handler.ServersPanelHandler;
import ikube.gui.panel.DashPanel;
import ikube.gui.panel.IndexesPanel;
import ikube.gui.panel.MenuPanel;
import ikube.gui.panel.SearchPanel;
import ikube.gui.panel.ServersPanel;
import ikube.util.ApplicationObjectSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
	private transient IndexesPanelContainer indexesPanelContainer;
	@Autowired
	private transient ApplicationObjectSupport applicationObjectSupport;
	@Autowired
	private transient ServersPanelContainer serversPanelContainer;

	public Window() {
		Window.INSTANCE = this;
		setName(IConstant.MAIN_WINDOW);
	}

	public void init() {
		VerticalLayout vertical = new VerticalLayout();
		vertical.setSizeFull();
		setContent(vertical);

		// Content on the bottom
		Panel dashPanel = new DashPanel();
		dashPanel.setDescription(IConstant.DASH);
		dashPanel.setData(dashPanelContainer);
		dashPanel.setVisible(true);
		dashPanel.setSizeFull();

		Panel searchPanel = new SearchPanel(/* indexOptionsContainer */);
		searchPanel.setDescription(IConstant.SEARCH);
		searchPanel.setData(searchPanelContainer);

		Panel indexesPanel = new IndexesPanel();
		indexesPanel.setDescription(IConstant.INDEXES);
		indexesPanel.setData(indexesPanelContainer);
		indexesPanel.setVisible(true);

		Panel serversPanel = new ServersPanel();
		serversPanel.setDescription(IConstant.INDEXING);
		serversPanel.setData(serversPanelContainer);

		// Menu panel
		Panel menuPanel = new MenuPanel(dashPanel, searchPanel, indexesPanel, serversPanel);
		menuPanel.setDescription(IConstant.MENU);
		vertical.addComponent(menuPanel);
		vertical.setExpandRatio(menuPanel, 1.0f);

		// Add the controllers/listeners/handlers to the panels
		new DashPanelHandler().registerHandler(dashPanel, dashPanelContainer);
		new SearchPanelHandler().registerHandler(searchPanel, searchPanelContainer);
		new IndexesPanelHandler().registerHandler(indexesPanel, indexesPanelContainer);
		new ServersPanelHandler().registerHandler(serversPanel, serversPanelContainer);
	}

}
