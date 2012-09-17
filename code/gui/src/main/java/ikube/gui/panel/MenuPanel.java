package ikube.gui.panel;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.gui.IConstant;
import ikube.gui.panel.wizard.EmailForm;
import ikube.gui.panel.wizard.IndexContextForm;
import ikube.service.IMonitorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
@Scope(value = "prototype")
@Component(value = "MenuPanel")
public class MenuPanel extends Panel {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(MenuPanel.class);

	private static final String IKUBE_CAPTION = "<b>Ikube</b>";

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	public MenuPanel(final Panel... panels) {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);

		AbsoluteLayout absoluteLayout = new AbsoluteLayout();

		addMenu(absoluteLayout);

		Label caption = new Label(IKUBE_CAPTION, Label.CONTENT_XHTML);
		caption.setStyleName(Reindeer.LABEL_H2);
		absoluteLayout.addComponent(caption, "left: 20px; top: 30px;");

		TabSheet tabSheet = new TabSheet();
		tabSheet.addStyleName(Reindeer.TABSHEET_MINIMAL);
		tabSheet.setStyleName(Reindeer.TABSHEET_MINIMAL);
		tabSheet.setDescription(IConstant.TAB_SHEET);

		for (Panel panel : panels) {
			tabSheet.addTab(panel, panel.getDescription());
		}

		absoluteLayout.addComponent(tabSheet, "left: 20px; top: 60px;");

		setContent(absoluteLayout);
	}

	private void addMenu(final AbsoluteLayout absoluteLayout) {
		// Save reference to individual items so we can add sub-menu items to them
		MenuBar menubar = new MenuBar();
		menubar.setWidth(100, Sizeable.UNITS_PERCENTAGE);

		Command menuCommand = new Command() {
			public void menuSelected(MenuItem selectedItem) {
				getWindow().showNotification("Action " + selectedItem.getText());
			}
		};

		final MenuBar.MenuItem index = menubar.addItem("Index", null);
		final MenuBar.MenuItem newIndex = index.addItem("New index", null);
		@SuppressWarnings("unused")
		final MenuBar.MenuItem deleteIndex = index.addItem("Delete index", menuCommand);

		addEmailForm(newIndex);

		newIndex.addItem("Internet", menuCommand);
		newIndex.addItem("File system", menuCommand);
		newIndex.addItem("Database", menuCommand);

		addIndexContextForm(newIndex);

		final MenuBar.MenuItem action = menubar.addItem("Action", null);
		action.addItem("Terminate indexing", menuCommand);
		action.addItem("Terminate all indexing", menuCommand).setEnabled(true);

		final MenuBar.MenuItem help = menubar.addItem("Help", null);
		help.addItem("Welcome", menuCommand);
		help.addSeparator();
		help.addItem("Contents", menuCommand);
		help.addItem("Search help", menuCommand);
		help.addSeparator();

		final MenuBar.MenuItem indexing = help.addItem("Indexing", menuCommand);
		indexing.addItem("E-mail", menuCommand);
		indexing.addItem("Internet", menuCommand);
		indexing.addItem("File system", menuCommand);
		indexing.addItem("Database", menuCommand);

		absoluteLayout.addComponent(menubar, "left: 0px; top: 0px;");
	}

	private void addIndexContextForm(final MenuBar.MenuItem menuItem) {
		menuItem.addItem("Index", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window window = new Window();
				window.setWidth(80, Sizeable.UNITS_PERCENTAGE);
				window.setHeight(80, Sizeable.UNITS_PERCENTAGE);
				Form indexContextForm = new IndexContextForm().initialize(window);
				window.addComponent(indexContextForm);
				ikube.gui.Window.INSTANCE.addWindow(window);
			}
		});
	}

	private void addEmailForm(final MenuBar.MenuItem menuItem) {
		menuItem.addItem("E-mail", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window window = new Window();
				window.setWidth(80, Sizeable.UNITS_PERCENTAGE);
				window.setHeight(80, Sizeable.UNITS_PERCENTAGE);
				Form emailForm = new EmailForm(window);
				window.addComponent(emailForm);
				ikube.gui.Window.INSTANCE.addWindow(window);
			}
		});
	}

}