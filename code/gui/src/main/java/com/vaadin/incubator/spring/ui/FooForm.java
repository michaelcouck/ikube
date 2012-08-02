package com.vaadin.incubator.spring.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.incubator.spring.model.Foo;
import com.vaadin.incubator.spring.service.FooService;
import com.vaadin.incubator.spring.util.BeanValidationForm;
import com.vaadin.incubator.spring.util.VaadinApplicationObjectSupport;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;

@Configurable(preConstruction = true)
public class FooForm extends BeanValidationForm<Foo> implements ClickListener {

	private static final long serialVersionUID = 1L;
	private final static Log logger = LogFactory.getLog(FooForm.class);

	@Autowired
	private transient FooService fooService;
	@Autowired
	private transient VaadinApplicationObjectSupport app;

	private List<FooModelListener> listeners = new ArrayList<FooModelListener>();

	private enum Buttons {
		SAVE, RESET;
	}

	public FooForm() {
		super(new Foo());
		setCaption(app.getMessage("fooform.caption"));
		setWriteThrough(false);
		TextField name = new TextField(app.getMessage("fooform.name"));
		name.setNullRepresentation("");
		addField("name", name);
		TextField number = new TextField(app.getMessage("fooform.number"));
		number.setNullRepresentation("");
		addField("number", number);
		DateField date = new DateField(app.getMessage("fooform.date"));
		date.setResolution(DateField.RESOLUTION_DAY);
		addField("date", date);
		HorizontalLayout footer = new HorizontalLayout();
		footer.setSpacing(true);
		Button save = new Button(app.getMessage("button.save"));
		save.addListener((ClickListener) this);
		save.setData(Buttons.SAVE);
		footer.addComponent(save);
		Button reset = new Button(app.getMessage("button.reset"));
		reset.addListener((ClickListener) this);
		reset.setData(Buttons.RESET);
		footer.addComponent(reset);
		setFooter(footer);
	}

	public void addFooModelListener(FooModelListener listener) {
		listeners.add(listener);
	}

	public void buttonClick(ClickEvent event) {
		if(logger.isDebugEnabled()) {
			logger.debug("buttonClick(" + event.getButton().getData() + ")");
		}
		if(event.getButton().getData() == Buttons.SAVE) {
			boolean valid = isValid();
			boolean modified = isModified();
			if(logger.isDebugEnabled()) {
				logger.debug("isModified: " + modified);
				logger.debug("isValid: " + valid);
			}
			if(modified && valid) {
				commit();
				Foo foo = getBeanItem().getBean();
				if(foo.getId() == null) {
					fooService.saveFoo(foo);
					for(FooModelListener listener : listeners) {
						listener.fooAdded(foo);
					}
					getApplication().getMainWindow().showNotification(app.getMessage("fooform.added"));
				} else {
					foo = fooService.updateFoo(foo);
					rebind(foo);
					for(FooModelListener listener : listeners) {
						listener.fooUpdated(foo);
					}
					getApplication().getMainWindow().showNotification(app.getMessage("fooform.updated"));
				}
			} else if(!valid) {
				getApplication().getMainWindow().showNotification(app.getMessage("fooform.notvalid"),
						Notification.TYPE_WARNING_MESSAGE);
			} else {
				getApplication().getMainWindow().showNotification(app.getMessage("fooform.notmodified"));
			}
		} else if(event.getButton().getData() == Buttons.RESET) {
			rebind(new Foo());
		}
	}

	public void update(Foo model) {
		if(logger.isDebugEnabled()) {
			logger.debug("updating form");
		}
		rebind(model);
	}
}
