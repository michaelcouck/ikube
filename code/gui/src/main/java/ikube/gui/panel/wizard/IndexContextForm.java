package ikube.gui.panel.wizard;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.service.IMonitorService;

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
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

@Configurable
@Scope(value = "prototype")
@org.springframework.stereotype.Component(value = "IndexContextForm")
public class IndexContextForm extends AForm {

	private static final Logger LOGGER = LoggerFactory.getLogger(IndexContextForm.class);

	@Autowired
	private IMonitorService monitorService;
	@Autowired
	private IClusterManager clusterManager;

	public Form initialize(final Window window) {
		setSizeFull();

		String[] fieldsNames = monitorService.getFieldNames(IndexContext.class);
		String[] fieldDescriptions = monitorService.getFieldDescriptions(IndexContext.class);

		setLayout(new GridLayout(3, fieldsNames.length + 2));
		getLayout().setSizeFull();

		for (int i = 0; i < fieldsNames.length; i++) {
			String fieldName = fieldsNames[i];
			String fieldDescription = fieldDescriptions[i];

			Label fieldNameLabel = new Label("<b>" + fieldName + "</b>", Label.CONTENT_XHTML);
			fieldNameLabel.setWidth(15, Sizeable.UNITS_PERCENTAGE);
			getLayout().addComponent(fieldNameLabel);

			TextField textField = new TextField();
			textField.setDescription(fieldName);
			textField.setWidth(90, Sizeable.UNITS_PERCENTAGE);
			getLayout().addComponent(textField);

			Label validationLabel = new Label(fieldDescription, Label.CONTENT_XHTML);
			validationLabel.setSizeFull();
			validationLabel.setDescription(fieldName);
			validationLabel.setData(fieldDescription);
			getLayout().addComponent(validationLabel);
		}
		addButton(window);
		return this;
	}

	private void addButton(final Window window) {
		Button.ClickListener clickListener = new Button.ClickListener() {
			@Override
			@SuppressWarnings("rawtypes")
			public void buttonClick(ClickEvent event) {
				// Perform adding the indexable
				IndexContext indexContext = new IndexContext();
				populateIndexable(IndexContextForm.this, indexContext);
				// LOGGER.info("Index context : " + ToStringBuilder.reflectionToString(indexContext, ToStringStyle.SHORT_PREFIX_STYLE));
				// Validate the form before sending
				Set<ConstraintViolation<IndexContext>> constraintViolations = validateIndexContext(indexContext);
				// LOGGER.info("Constraint violations : " + constraintViolations.size());
				// LOGGER.info("Constraint violations : " + constraintViolations);
				if (constraintViolations == null || constraintViolations.size() == 0) {
					clusterManager.sendMessage(indexContext);
					ikube.gui.Window.INSTANCE.removeWindow(window);
					return;
				}
				IndexContextForm.this.getLayout().requestRepaintAll();
			}
		};
		Button button = new Button("Add", clickListener);
		getFooter().addComponent(button);
	}

	@SuppressWarnings("rawtypes")
	private Set<ConstraintViolation<IndexContext>> validateIndexContext(final IndexContext indexContext) {
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
		Set<ConstraintViolation<IndexContext>> constraintViolations = validator.validate(indexContext);
		for (ConstraintViolation<IndexContext> constraintViolation : constraintViolations) {
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
						// LOGGER.info("Property path : " + constraintViolation.getPropertyPath() + ", label : " + label.getDescription());
						StringBuilder content = new StringBuilder();
						content.append(label.getData());
						content.append("<br>");
						content.append("<b>");
						content.append(constraintViolation.getMessage());
						content.append("</b>");
						label.setValue(content.toString());
					}
				}
			}
		}
		return constraintViolations;
	}

}