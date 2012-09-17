package ikube.gui.panel.wizard;

import ikube.model.Indexable;

import java.util.Iterator;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextField;

public abstract class AForm extends Form {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected void populateIndexable(final Form form, final Indexable<?> indexable) {
		Iterator<Component> iterator = form.getLayout().getComponentIterator();
		while (iterator.hasNext()) {
			Component component = iterator.next();
			if (TextField.class.isAssignableFrom(component.getClass())) {
				TextField textField = (TextField) component;
				String fieldName = textField.getDescription();
				Object fieldValue = textField.getValue();
				try {
					logger.info("Setting field : " + fieldName + ", " + fieldValue);
					BeanUtils.setProperty(indexable, fieldName, fieldValue);
				} catch (Exception e) {
					logger.error("Exception setting indexable field : " + fieldName + ", value : " + fieldValue, e);
				}
			}
		}
	}

}
