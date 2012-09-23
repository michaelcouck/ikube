package ikube.gui.panel;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.gui.IConstant;
import ikube.gui.panel.wizard.IndexableForm;
import ikube.listener.ListenerManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableEmail;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableInternet;
import ikube.model.IndexableTable;
import ikube.service.IMonitorService;
import ikube.toolkit.ObjectToolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.dialogs.ConfirmDialog;

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

		addNewIndexableForm(newIndex, "Email", new IndexableEmail());
		addNewIndexableForm(newIndex, "Internet site", new IndexableInternet());
		addNewIndexableForm(newIndex, "File system", new IndexableFileSystem());
		addNewIndexableForm(newIndex, "Database table", new IndexableTable());
		addNewIndexableForm(newIndex, "Collection", new IndexContext<Object>());

		final MenuBar.MenuItem action = menubar.addItem("Action", null);
		addRestartIndexingItem(action);
		addTerminateIndexingItem(action);

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

	private void addTerminateIndexingItem(final MenuBar.MenuItem menuItem) {
		class DialogListener implements ConfirmDialog.Listener {
			@Override
			public void onClose(ConfirmDialog confirmDialog) {
				if (confirmDialog.isConfirmed()) {
					long time = System.currentTimeMillis();
					ikube.listener.Event terminateEvent = ListenerManager.getEvent(ikube.listener.Event.TERMINATE_ALL, time, null,
							Boolean.FALSE);
					LOGGER.info("Sending terminate event : " + terminateEvent);
					clusterManager.sendMessage(terminateEvent);
				}
			}
		}
		menuItem.addItem("Terminate indexing", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				String caption = "Terminate indexing";
				String message = "Are you sure you want to terminate indexing for this server";
				ConfirmDialog.show(ikube.gui.Window.INSTANCE, caption, message, "Yes", "No", new DialogListener());
			}
		});
	}

	private void addRestartIndexingItem(final MenuBar.MenuItem menuItem) {
		class DialogListener implements ConfirmDialog.Listener {
			@Override
			public void onClose(ConfirmDialog confirmDialog) {
				if (confirmDialog.isConfirmed()) {
					long time = System.currentTimeMillis();
					ikube.listener.Event terminateEvent = ListenerManager.getEvent(ikube.listener.Event.STARTUP_ALL, time, null,
							Boolean.FALSE);
					LOGGER.info("Sending startup event : " + terminateEvent);
					clusterManager.sendMessage(terminateEvent);
				}
			}
		}
		menuItem.addItem("Restart indexing", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				String caption = "Restart indexing";
				String message = "Are you sure you want to restart indexing for this server";
				ConfirmDialog.show(ikube.gui.Window.INSTANCE, caption, message, "Yes", "No", new DialogListener());
			}
		});
	}

	private void addNewIndexableForm(final MenuBar.MenuItem menuItem, final String indexableName, final Indexable<?> indexable) {
		menuItem.addItem(indexableName, new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				final Window window = new Window();
				window.setWidth(80, Sizeable.UNITS_PERCENTAGE);
				window.setHeight(80, Sizeable.UNITS_PERCENTAGE);
				ObjectToolkit.populateFields(indexable.getClass(), indexable, false, 0, 3);
				Form indexContextForm = new IndexableForm().initialize(window, indexable);
				window.addComponent(indexContextForm);
				ikube.gui.Window.INSTANCE.addWindow(window);
			}
		});
	}

}