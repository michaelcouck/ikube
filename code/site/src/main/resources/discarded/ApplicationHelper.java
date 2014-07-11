package com.vaadin.incubator.spring.discarded;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;

public class ApplicationHelper {
	
	public static ApplicationContext getApplicationContext() {
		return getApplicationContext(getCurrentApplication());
	}
	
	/*
	 * For Terracotta. ThreadLocal is JVM specific so we can't use method getCurrentApplication().
	 * See tc-config.xml <on-load> elements.
	 */
	public static ApplicationContext getApplicationContext(Application app) {
		ServletContext sc = ((WebApplicationContext)app.getContext()).getHttpSession().getServletContext();
		return WebApplicationContextUtils.getWebApplicationContext(sc);
	}
	
	public static WebBrowser getBrowser() {
		Application app = getCurrentApplication();
		return ((WebApplicationContext)app.getContext()).getBrowser();
	}

	public static HttpServletRequest getRequest() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if(attrs != null) {
			return attrs.getRequest();
		}
		return null;
	}
	
	public static Application getCurrentApplication() {
		return ApplicationHolder.getApplication();
	}

	public static Locale getCurrentLocale() {
		return getCurrentApplication().getLocale();
	}
	
	public static String getMessage(String key) {
		// return getVaadinApplicationObjectSupport().getMessage(key);
		return null;
	}
	
	public static String getMessage(String key, String... args) {
		// return getVaadinApplicationObjectSupport().getMessage(key, args);
		return null;
	}

//	public static ApplicationObjectSupport getVaadinApplicationObjectSupport() {
//		String names[] = getApplicationContext().getBeanNamesForType(ApplicationObjectSupport.class);
//		if(names != null && names.length == 1) {
//			return (ApplicationObjectSupport) getApplicationContext().getBean(names[0]);
//		}
//		return null;
//	}
}
