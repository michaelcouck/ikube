package ikube.gui.panel;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.gui.IConstant;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableEmail;
import ikube.service.IMonitorService;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@Configurable
@Scope(value = "prototype")
@Component(value = "MenuPanel")
public class MenuPanel extends Panel {

	private static final Logger LOGGER = LoggerFactory.getLogger(MenuPanel.class);

	private static final String IKUBE_CAPTION = "<b>Ikube</b>";

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;
	@Autowired
	private IDataBase dataBase;

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
				final Form form = new Form();
				form.setSizeFull();
				String[] fields = monitorService.getFieldNames(IndexContext.class);
				for (String field : fields) {
					// TODO Add column definition to the form
					form.addField(field, new TextField(field + " : ", ""));
				}
				Button button = new Button("Add", new Button.ClickListener() {
					@Override
					@SuppressWarnings("rawtypes")
					public void buttonClick(ClickEvent event) {
						// Perform adding the indexable
						IndexContext indexContext = new IndexContext();
						populateIndexable(form, indexContext);
						clusterManager.sendMessage(indexContext);
						ikube.gui.Window.INSTANCE.removeWindow(window);
					}
				});
				form.getFooter().addComponent(button);
				window.addComponent(form);
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
				final Form form = new Form();
				form.setSizeFull();
				final TextField indexNameField = new TextField("Index name : ", "index name here");
				form.addField("indexName", indexNameField);

				ComboBox indexContextComboBox = new ComboBox();
				indexContextComboBox.setImmediate(true);
				for (String indexName : monitorService.getIndexNames()) {
					indexContextComboBox.addItem(indexName);
				}

				String[] fields = monitorService.getFieldNames(IndexableEmail.class);
				for (String field : fields) {
					// TODO Add column definition to the form
					form.addField(field, new TextField(field + " : ", ""));
				}
				Button button = new Button("Add", new Button.ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						// Perform adding the indexable
						String indexName = indexNameField.getValue().toString();
						String[] fieldNames = new String[] { "name" };
						Object[] fieldValues = new Object[] { indexName };
						IndexContext<?> indexContext = dataBase
								.find(IndexContext.class, IndexContext.FIND_BY_NAME, fieldNames, fieldValues);
						IndexableEmail indexableEmail = new IndexableEmail();
						populateIndexable(form, indexableEmail);
						indexContext.getChildren().add(indexableEmail);
						clusterManager.sendMessage(indexContext);
						ikube.gui.Window.INSTANCE.removeWindow(window);
					}
				});
				form.getFooter().addComponent(button);
				window.addComponent(form);
				ikube.gui.Window.INSTANCE.addWindow(window);
			}
		});
	}

	private void populateIndexable(final Form form, final Indexable<?> indexable) {
		final Collection<?> propertyIds = form.getItemPropertyIds();
		ReflectionUtils.doWithFields(indexable.getClass(), new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				String fieldName = field.getName();
				if (propertyIds.contains(fieldName)) {
					Object fieldValue = form.getField(fieldName).getValue();
					try {
						BeanUtils.setProperty(indexable, fieldName, fieldValue);
					} catch (InvocationTargetException e) {
						LOGGER.error("Exception setting indexable field : " + field + ", " + fieldValue, e);
					}
				}
			}
		});
	}

}