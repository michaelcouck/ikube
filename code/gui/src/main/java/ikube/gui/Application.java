package ikube.gui;

import ikube.gui.data.IndexPanelContainer;
import ikube.gui.data.IndexesPanelContainer;
import ikube.gui.data.NavigationPanelContainer;
import ikube.gui.handler.DashboardPanelHandler;
import ikube.gui.handler.IndexPanelHandler;
import ikube.gui.handler.IndexesPanelHandler;
import ikube.gui.handler.NavigationPanelHandler;
import ikube.gui.panel.DashPanel;
import ikube.gui.panel.IndexesPanel;
import ikube.gui.panel.MenuPanel;
import ikube.gui.panel.NavigationPanel;
import ikube.util.ApplicationObjectSupport;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Scope(value = "prototype")
@Component(value = "application")
public class Application extends com.vaadin.Application implements HttpServletRequestListener {

	class LayoutWindow extends Window {
		LayoutWindow() {
			// Our main layout is a horizontal layout
			VerticalLayout vertical = new VerticalLayout();
			vertical.setSizeFull();
			// vertical.setLocked(Boolean.TRUE);
			// vertical.setSplitPosition(80, Sizeable.UNITS_PIXELS);
			setContent(vertical);

			// Menu panel
			Panel menuPanel = new MenuPanel();
			menuPanel.setDescription(IConstant.MENU);
			vertical.addComponent(menuPanel);
			vertical.setExpandRatio(menuPanel, 0.1f);

			// Vertically divide the right area
			HorizontalSplitPanel horizontal = new HorizontalSplitPanel();
			// horizontal.setSizeFull();
			horizontal.setSplitPosition(200, Sizeable.UNITS_PIXELS);
			vertical.addComponent(horizontal);
			vertical.setExpandRatio(horizontal, 0.9f);

			// We need this panel to init the navigator
			Panel dashboardPanel = new DashPanel();
			// Navigation tree to the left
			Panel navigationPanel = new NavigationPanel();
			navigationPanel.setData(navigationPanelContainer);
			navigationPanel.setDescription(IConstant.NAVIGATION);
			new NavigationPanelHandler(dashboardPanel).addListener(navigationPanel);
			horizontal.addComponent(navigationPanel);

			// Content on the bottom
			dashboardPanel.setDescription(IConstant.DASHBOARD);
			new DashboardPanelHandler().addListener(dashboardPanel);
			horizontal.addComponent(dashboardPanel);

			Panel indexesPanel = new IndexesPanel();
			indexesPanel.setDescription(IConstant.INDEXES);
			indexesPanel.setData(indexesPanelContainer);
			new IndexesPanelHandler().addListener(indexesPanel);
			indexesPanel.setVisible(true);

			Panel indexPanel = new IndexesPanel();
			indexPanel.setDescription(IConstant.INDEX);
			indexPanel.setData(indexPanelContainer);
			new IndexPanelHandler().addListener(indexPanel);
		}
	}

	private transient static Application APPLICATION;

	@Autowired
	private transient IndexPanelContainer indexPanelContainer;
	@Autowired
	private transient IndexesPanelContainer indexesPanelContainer;
	@Autowired
	private transient ApplicationObjectSupport applicationObjectSupport;
	@Autowired
	private transient NavigationPanelContainer navigationPanelContainer;

	public static final Application getApplication() {
		return Application.APPLICATION;
	}

	public void init() {
		Application.APPLICATION = this;
		setTheme(Reindeer.THEME_NAME);
		Window mainWindow = new LayoutWindow();
		mainWindow.setName(IConstant.MAIN_WINDOW);
		setMainWindow(mainWindow);
		// Set the theme and style for every component in the window hierarchy
		Styler.setThemeAndStyle(Reindeer.THEME_NAME, Reindeer.LAYOUT_WHITE, getWindows());
	}

	@Override
	public void terminalError(Terminal.ErrorEvent event) {
		getMainWindow().showNotification(applicationObjectSupport.getMessage("accessdenied"), Notification.TYPE_ERROR_MESSAGE);
	}

	@Override
	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
	}

	@Override
	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
	}

	@Override
	public Locale getLocale() {
		return LocaleContextHolder.getLocale();
	}

	@Override
	public void setLocale(Locale locale) {
		LocaleContextHolder.setLocale(locale);
	}

}