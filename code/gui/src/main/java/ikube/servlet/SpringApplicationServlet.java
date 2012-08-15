package ikube.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

@SuppressWarnings("serial")
public class SpringApplicationServlet extends AbstractApplicationServlet {

	private static final Log logger = LogFactory.getLog(SpringApplicationServlet.class);

	private WebApplicationContext applicationContext;
	private Class<? extends Application> applicationClass;
	private String applicationBean;
	private LocaleResolver localeResolver;

	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		applicationBean = servletConfig.getInitParameter("applicationBean");
		if (applicationBean == null) {
			throw new ServletException("ApplicationBean not specified in servlet parameters");
		}
		applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletConfig.getServletContext());
		applicationClass = (Class<? extends Application>) applicationContext.getType(applicationBean);
		initLocaleResolver(applicationContext);
	}

	private void initLocaleResolver(ApplicationContext context) {
		try {
			this.localeResolver = (LocaleResolver) context.getBean(DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
		} catch (NoSuchBeanDefinitionException ex) {
			this.localeResolver = new SessionLocaleResolver();
			logger.error("Unable to locate LocaleResolver with name '" + DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME + "' using default ["
					+ localeResolver + "]");
		}
	}

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final Locale locale = localeResolver.resolveLocale(request);
		LocaleContextHolder.setLocale(locale);
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);
		try {
			super.service(new HttpServletRequestWrapper(request) {
				@Override
				public Locale getLocale() {
					return locale;
				}
			}, response);
		} finally {
			if (!locale.equals(LocaleContextHolder.getLocale())) {
				logger.debug("locale changed, updating locale resolver");
				localeResolver.setLocale(request, response, LocaleContextHolder.getLocale());
			}
			LocaleContextHolder.resetLocaleContext();
			RequestContextHolder.resetRequestAttributes();
		}
	}

	@Override
	protected Application getNewApplication(HttpServletRequest request) throws ServletException {
		logger.trace("getNewApplication()");
		return (Application) applicationContext.getBean(applicationBean);
	}

	@Override
	protected Class<? extends Application> getApplicationClass() throws ClassNotFoundException {
		logger.trace("getApplicationClass()");
		return applicationClass;
	}

}
