package ikube.gui;

import ikube.gui.toolkit.Styler;
import ikube.util.ApplicationObjectSupport;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;

@Scope(value = "prototype")
@Component(value = "application")
public class Application extends com.vaadin.Application implements HttpServletRequestListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private transient static Application APPLICATION;

	@Autowired
	private transient Window window;
	@Autowired
	private transient ApplicationObjectSupport applicationObjectSupport;

	public static final Application getApplication() {
		return Application.APPLICATION;
	}

	public void init() {
		Application.APPLICATION = this;
		setTheme(Reindeer.THEME_NAME);
		window.init();
		setMainWindow(window);
		// Set the theme and style for every component in the window hierarchy
		Styler.setThemeAndStyle(Reindeer.THEME_NAME, Reindeer.WINDOW_LIGHT, getWindows());
	}

	@Override
	public void terminalError(Terminal.ErrorEvent event) {
		LOGGER.error("Terminal error : ", event.getThrowable());
		String message = applicationObjectSupport.getMessage("accessdenied");
		getMainWindow().showNotification(message, Notification.TYPE_ERROR_MESSAGE);
	}

	@Override
	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
		// LOGGER.info("Request start : ");
	}

	@Override
	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
		// LOGGER.info("Request end : ");
	}

	@Override
	public Locale getLocale() {
		return LocaleContextHolder.getLocale();
	}

	@Override
	public void setLocale(Locale locale) {
		LocaleContextHolder.setLocale(locale);
	}

}