package com.vaadin.incubator.spring.ui;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.vaadin.incubator.spring.ui.panel.ContentPanel;
import com.vaadin.incubator.spring.ui.panel.NavigationPanel;
import com.vaadin.incubator.spring.ui.panel.TopPanel;
import com.vaadin.incubator.spring.util.ApplicationObjectSupport;
import com.vaadin.incubator.spring.util.BaseApplication;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@Scope(value = "prototype")
@Component(value = "fooApplication")
public class FooApplication extends BaseApplication implements ClickListener {

	@Autowired
	private transient ApplicationObjectSupport applicationObjectSupport;

	private enum Buttons {
		LOGOUT, LANGUAGE;
	}

	public void init() {
		Window mainWindow = new ApplicationLayoutWindow();
		setMainWindow(mainWindow);
	}

	@Override
	public void terminalError(Terminal.ErrorEvent event) {
		if (event.getThrowable().getCause() instanceof AccessDeniedException) {
			getMainWindow().showNotification(applicationObjectSupport.getMessage("accessdenied"), Notification.TYPE_ERROR_MESSAGE);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton().getData() == Buttons.LOGOUT) {
			String context = getURL().getPath();
			context = context.substring(0, context.lastIndexOf("/applicationObjectSupport"));
			getMainWindow().open(new ExternalResource(context + "/j_spring_security_logout"));
		} else if (event.getButton().getData() == Buttons.LANGUAGE) {
			setLocale((Locale.US.equals(getLocale())) ? new Locale("fi", "FI") : Locale.US);
			close();
		}
	}

	class ApplicationLayoutWindow extends Window {
		ApplicationLayoutWindow() {
			// Our main layout is a horizontal layout
			HorizontalLayout main = new HorizontalLayout();
			main.setSizeFull();
			setContent(main);

			// Navigation tree to the left
			Panel treePanel = new NavigationPanel(); // for scrollbars
			addComponent(treePanel);

			// Vertically divide the right area
			VerticalLayout left = new VerticalLayout();
			left.setSizeFull();
			addComponent(left);
			main.setExpandRatio(left, 1.0f); // use all available space

			// Top panel
			Panel textPanel = new TopPanel(); // for scrollbars
			left.addComponent(textPanel);
			left.setExpandRatio(textPanel, 0.10f);

			// Content on the bottom
			Panel contentPanel = new ContentPanel();
			left.addComponent(contentPanel);
			left.setExpandRatio(contentPanel, 0.90f);
		}

	}

}