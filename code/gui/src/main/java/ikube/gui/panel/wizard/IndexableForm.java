package ikube.gui.panel.wizard;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.service.IMonitorService;
import ikube.toolkit.ObjectToolkit;

import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Scope;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@Configurable
@Scope(value = "prototype")
@org.springframework.stereotype.Component(value = "IndexableForm")
public class IndexableForm extends AForm {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexableForm.class);

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Form initialize(final Window window, final Indexable indexable) {
		setSizeFull();

		String[] fieldsNames = monitorService.getFieldNames(indexable.getClass());
		String[] fieldDescriptions = monitorService.getFieldDescriptions(indexable.getClass());

		GridLayout gridLayout = new GridLayout(3, fieldsNames.length + 2);
		gridLayout.setSizeFull();
		setLayout(gridLayout);

		Label notImplementedLabel = new Label("<font size='2' color='red'><b>Adding indexables is via Spring XML. <br>"
				+ "Funtionality to add indexables in the GUI will be available in the next release, sorry :)</b></font>",
				Label.CONTENT_XHTML);
		gridLayout.addComponent(notImplementedLabel, 0, 0, 2, 0);

		Label indexesLabel = new Label("<font size='2'><b>Add indexable to collection : </b></font>", Label.CONTENT_XHTML);
		gridLayout.addComponent(indexesLabel);

		ComboBox comboBox = new ComboBox();
		for (final IndexContext<?> indexContext : monitorService.getIndexContexts().values()) {
			comboBox.addItem(indexContext.getIndexName());
		}

		gridLayout.addComponent(comboBox, 1, 1, 2, 1);

		for (int i = 0; i < fieldsNames.length; i++) {
			String fieldName = fieldsNames[i];
			String fieldDescription = fieldDescriptions[i];

			Label fieldNameLabel = new Label("<b>" + fieldName + "</b>", Label.CONTENT_XHTML);
			fieldNameLabel.setWidth(15, Sizeable.UNITS_PERCENTAGE);
			gridLayout.addComponent(fieldNameLabel);

			Object fieldValue = ObjectToolkit.getFieldValue(indexable, fieldName);
			TextField textField = new TextField();
			textField.setDescription(fieldName);
			textField.setWidth(90, Sizeable.UNITS_PERCENTAGE);
			textField.setValue(fieldValue);
			gridLayout.addComponent(textField);

			Label validationLabel = new Label(fieldDescription, Label.CONTENT_XHTML);
			validationLabel.setSizeFull();
			validationLabel.setDescription(fieldName);
			validationLabel.setData(fieldDescription);
			gridLayout.addComponent(validationLabel);
		}
		// addUpdateButton(window, "Add", (Class<Indexable>) indexable.getClass());
		// addUpdateButton(window, "Update", (Class<Indexable>) indexable.getClass());
		cancelButton(window, "Cancel", (Class<Indexable>) indexable.getClass());
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private void addUpdateButton(final Window window, final String name, final Class<Indexable> klass) {
		Button.ClickListener clickListener = new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				// Perform adding the indexable
				Indexable indexable;
				try {
					indexable = klass.newInstance();
				} catch (Exception e) {
					LOGGER.error("Exception initializing the indexable : ", e);
					return;
				}
				populateIndexable(IndexableForm.this, indexable);
				// Validate the form before sending
				Set<ConstraintViolation<Indexable>> constraintViolations = validateIndexContext(indexable);
				if (constraintViolations == null || constraintViolations.size() == 0) {
					clusterManager.sendMessage(indexable);
					ikube.gui.Window.INSTANCE.removeWindow(window);
					return;
				}
				IndexableForm.this.getLayout().requestRepaintAll();
			}
		};
		Button button = new Button(name, clickListener);
		getFooter().addComponent(button);
	}

	@SuppressWarnings({ "rawtypes" })
	private void cancelButton(final Window window, final String name, final Class<Indexable> klass) {
		Button.ClickListener clickListener = new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ikube.gui.Window.INSTANCE.removeWindow(window);
			}
		};
		Button button = new Button(name, clickListener);
		getFooter().addComponent(button);
	}

	@SuppressWarnings("rawtypes")
	private Set<ConstraintViolation<Indexable>> validateIndexContext(final Indexable indexContext) {
		// Reset the labels to the original text less the validation message
		Iterator<Component> iterator = getLayout().getComponentIterator();
		while (iterator.hasNext()) {
			Component component = iterator.next();
			if (Label.class.isAssignableFrom(component.getClass())) {
				Label label = (Label) component;
				if (label.getDescription() == null) {
					continue;
				}
				LOGGER.info("Resetting label : " + label.getData());
				label.setValue(label.getData());
			}
		}
		ValidatorFactory validationFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validationFactory.getValidator();
		Set<ConstraintViolation<Indexable>> constraintViolations = validator.validate(indexContext);
		for (ConstraintViolation<Indexable> constraintViolation : constraintViolations) {
			iterator = getLayout().getComponentIterator();
			while (iterator.hasNext()) {
				Component component = iterator.next();
				if (Label.class.isAssignableFrom(component.getClass())) {
					Label label = (Label) component;
					if (label.getDescription() == null) {
						continue;
					}
					Path path = constraintViolation.getPropertyPath();
					if (label.getDescription().equals(path.toString())) {
						// label.setCaption("<b color='red'>" + constraintViolation.getMessage() + "</b>");
						StringBuilder content = new StringBuilder();
						content.append("<font color='red'><b>");
						content.append(constraintViolation.getMessage());
						content.append("</b></font>");
						content.append("<br>");
						content.append(label.getData());
						label.setValue(content.toString());
					}
				}
			}
		}
		return constraintViolations;
	}

}