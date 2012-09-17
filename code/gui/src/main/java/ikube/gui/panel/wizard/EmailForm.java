package ikube.gui.panel.wizard;

import ikube.cluster.IClusterManager;
import ikube.model.IndexableEmail;
import ikube.service.IMonitorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@Configurable
@Scope(value = "prototype")
@Component(value = "EmailForm")
public class EmailForm extends AForm {

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	public EmailForm(final Window window) {
		setSizeFull();

		String[] fields = monitorService.getFieldNames(IndexableEmail.class);
		setLayout(new GridLayout(2, fields.length + 3));

		final ComboBox indexContextComboBox = new ComboBox();
		indexContextComboBox.setImmediate(true);
		for (String indexName : monitorService.getIndexNames()) {
			indexContextComboBox.addItem(indexName);
		}
		addField("indexName", indexContextComboBox);

		String[] descriptions = monitorService.getFieldDescriptions(IndexableEmail.class);
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			String description = descriptions[i];
			TextField textField = new TextField(field + " : ", description);
			textField.setWidth(150, Sizeable.UNITS_PIXELS);
			addField(field, textField);
		}
		Button button = new Button("Add", new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				IndexableEmail indexableEmail = new IndexableEmail();
				populateIndexable(EmailForm.this, indexableEmail);
				clusterManager.sendMessage(indexableEmail);
				ikube.gui.Window.INSTANCE.removeWindow(window);
			}
		});
		getFooter().addComponent(button);
	}

}