package ikube.gui.panel;

import ikube.gui.IConstant;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.Reindeer;

public class MenuPanel extends Panel {

	private static final String IKUBE_CAPTION = "<b>Ikube</b>";

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

		final MenuBar.MenuItem file = menubar.addItem("File", null);
		final MenuBar.MenuItem newItem = file.addItem("New", null);
		file.addItem("Open file...", menuCommand);
		file.addSeparator();

		newItem.addItem("File", menuCommand);
		newItem.addItem("Folder", menuCommand);
		newItem.addItem("Project...", menuCommand);

		file.addItem("Close", menuCommand);
		file.addItem("Close All", menuCommand);
		file.addSeparator();

		file.addItem("Save", menuCommand);
		file.addItem("Save As...", menuCommand);
		file.addItem("Save All", menuCommand);

		final MenuBar.MenuItem edit = menubar.addItem("Edit", null);
		edit.addItem("Undo", menuCommand);
		edit.addItem("Redo", menuCommand).setEnabled(false);
		edit.addSeparator();

		edit.addItem("Cut", menuCommand);
		edit.addItem("Copy", menuCommand);
		edit.addItem("Paste", menuCommand);
		edit.addSeparator();

		final MenuBar.MenuItem find = edit.addItem("Find/Replace", menuCommand);

		// Actions can be added inline as well, of course
		find.addItem("Google Search", new Command() {
			public void menuSelected(MenuItem selectedItem) {
				getWindow().open(new ExternalResource("http://www.google.com"));
			}
		});
		find.addSeparator();
		find.addItem("Find/Replace...", menuCommand);
		find.addItem("Find Next", menuCommand);
		find.addItem("Find Previous", menuCommand);

		final MenuBar.MenuItem view = menubar.addItem("View", null);
		view.addItem("Show/Hide Status Bar", menuCommand);
		view.addItem("Customize Toolbar...", menuCommand);
		view.addSeparator();

		view.addItem("Actual Size", menuCommand);
		view.addItem("Zoom In", menuCommand);
		view.addItem("Zoom Out", menuCommand);

		absoluteLayout.addComponent(menubar, "left: 0px; top: 0px;");
	}

}