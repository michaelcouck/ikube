package com.vaadin.incubator.spring.ui;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.vaadin.incubator.spring.model.Foo;
import com.vaadin.incubator.spring.util.ApplicationHelper;
import com.vaadin.incubator.spring.util.BaseApplication;
import com.vaadin.incubator.spring.util.VaadinApplicationObjectSupport;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;

@Component(value = "fooApplication")
@Scope(value = "prototype")
public class FooApplication extends BaseApplication implements ClickListener, FooClickListener, FooModelListener {

	private static final long serialVersionUID = 1L;
	private final static Log logger = LogFactory.getLog(FooApplication.class);

	private FooForm form;
	private FooTable table;
	@Autowired
	private transient VaadinApplicationObjectSupport app;

	private enum Buttons {
		LOGOUT, LANGUAGE;
	}

	public void init() {
		if(logger.isDebugEnabled()) {
			logger.debug("init, locale: " + getLocale());
		}
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		Window mainWindow = new Window(app.getMessage("application.name"), layout);
		Button logout = new Button(app.getMessage("button.logout"));
		logout.setStyleName(Button.STYLE_LINK);
		logout.addListener(this);
		logout.setData(Buttons.LOGOUT);
		mainWindow.addComponent(logout);
		//try application helper, same as app.getMessage()
		Button language = new Button(ApplicationHelper.getMessage("button.changelanguage"));
		language.setStyleName(Button.STYLE_LINK);
		language.addListener(this);
		language.setData(Buttons.LANGUAGE);
		mainWindow.addComponent(language);
		mainWindow.addComponent(createForm());
		mainWindow.addComponent(createTable());
		setMainWindow(mainWindow);
		if(logger.isDebugEnabled()) {
			logger.debug("initialization done");
		}
	}

	private Table createTable() {
		table = new FooTable();
		table.addFooClickListener(this);
		return table;
	}

	private Form createForm() {
		form = new FooForm();
		form.addFooModelListener(this);
		return form;
	}

	@Override
	public void terminalError(com.vaadin.terminal.Terminal.ErrorEvent event) {
		logger.error(event.getThrowable().getMessage(), event.getThrowable());
		if(event.getThrowable().getCause() instanceof AccessDeniedException) {
			getMainWindow().showNotification(app.getMessage("accessdenied"), Notification.TYPE_ERROR_MESSAGE);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if(logger.isDebugEnabled()) {
			logger.debug("buttonClick(" + event.getButton().getData() + ")");
		}
		if(event.getButton().getData() == Buttons.LOGOUT) {
			String context = getURL().getPath();
			context = context.substring(0, context.lastIndexOf("/app"));
			getMainWindow().open(new ExternalResource(context + "/j_spring_security_logout"));
		} else if(event.getButton().getData() == Buttons.LANGUAGE) {
			setLocale((Locale.US.equals(getLocale())) ? new Locale("fi", "FI") : Locale.US);
			close();
		}
	}

	@Override
	public void fooClicked(Foo model) {
		form.update(model);
	}

	@Override
	public void fooAdded(Foo model) {
		table.refresh();
	}

	@Override
	public void fooUpdated(Foo model) {
		table.refresh();
	}
}
